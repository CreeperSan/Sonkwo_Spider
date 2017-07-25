import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class SpiderController(window: MainWindow) {
    private val messageCenter:MessageCenter
    private val urlCenter:UrlCenter
    private val spider:Spider
    private val processCenter:ProcessCenter
    //状态字
    private var isRunning = false   //是否正在运行
    get
    private var isPausing = false   //是否已经暂停
    get

    init {
        messageCenter = MessageCenter(window)
        urlCenter = UrlCenter()
        processCenter = ProcessCenter()
        spider = Spider(urlCenter,messageCenter)
    }

    fun start(from:Int,to:Int){
        urlCenter.init(from, to)
        spider.start()
    }


    inner class ProcessCenter{

    }

    /*******************************************************************************************************************
     *          往下为消息中心
     */
    inner class MessageCenter(mainWindow: MainWindow) {
        private var window:MainWindow = mainWindow

        fun sendMessage(message:String){
            window.addMessage(message)
        }

        fun sendCurrentMessage(progress:Int,message: String){
            window.addCurrentMessage(progress,message)
        }

        fun sendTotalMessage(progress: Int,message: String){
            window.addTotalMessage(progress, message)
        }

        fun sendTotalMessage(message: String){
            window.addTotalMessage(message)
        }

        fun sendToDatabase(title:String,sell:String,price:String,discount:String,steam:Boolean,img:String,video:String,summary:String,keyword:String){
            window.insertIntoDatabase(title, sell, price, discount, steam, img, video, summary, keyword)
        }
    }


    /*******************************************************************************************************************
     *          往下为链接分配
     */

    inner class UrlCenter{
        private val badLinkList:ArrayList<String>   //请求或者解析失败的链接集合
        get
        private var current = 0
        private var from = 0
        private var to = 0

        init {
            badLinkList = ArrayList<String>()
        }

        fun init(from: Int,to: Int){
            current = from
            this.from = from
            this.to = to
        }

        fun requestUrl():String{
            if (current>to || current<from){
                return ""
            }
            return "https://www.sonkwo.com/products/${current++}"
        }

        fun reportBadLink(link:String){
            badLinkList.add(link)
        }

    }

    /*******************************************************************************************************************
     *          往下为爬虫
     */

    inner class Spider(private var urlCenter: UrlCenter, private var messageCenter: MessageCenter):Thread() {
        private var titleFormat = "<h2>.+</h2><p>"                                     //匹配游戏标题=
        private var keyWordFormat = "<meta content=.+ name=\"keywords\" />"     //匹配关键字=
        private var summaryFormat = "<div class=\"game-summary\">.+</div><div class=\"game-version-block\"></div>"      //匹配介绍=
        private var discountFormat = "<span class=\"discount\">.+</span><span class=\"sale-price\">"       //匹配折扣=
        private var salePriceFormat = "<span class=\"sale-price\">.+</span><span class=\"list-price\">"    //匹配售价=
        private var listPriceFormat = "<span class=\"list-price\">.+</span></div>"    //匹配原价=
        private var needSteamFormat = "【Steam】本游戏运行需通过"                   //匹配是否需要Steam=
        private var titleImageFormat = "<img height=\"260\" src=\".+\" width=\"440\" />"                      //匹配游戏标题图片=
        private var previewVideoFormat = "<source src=\".+\" type=\"video/mp4\" /><p class=\"vjs-no-js\">"                            //匹配游戏视频=
        private val titleCompiler = Pattern.compile(titleFormat)
        private val keyWordCompiler = Pattern.compile(keyWordFormat)
        private val summaryCompiler = Pattern.compile(summaryFormat)
        private val discountCompiler = Pattern.compile(discountFormat)
        private val salePriceCompiler = Pattern.compile(salePriceFormat)
        private val listPriceCompiler = Pattern.compile(listPriceFormat)
        private val needSteamCompiler = Pattern.compile(needSteamFormat)
        private val titleImageCompiler = Pattern.compile(titleImageFormat)
        private val previewVideoCompiler = Pattern.compile(previewVideoFormat)

        private var currentUrl = ""

        init {

        }

        override fun run(){
            super.run()
            while (true){
                currentUrl = urlCenter.requestUrl()
                if (currentUrl == ""){
                    break
                }
                messageCenter.sendCurrentMessage(1,"请求Url")
                val numStr = currentUrl.substring(32)
                messageCenter.sendTotalMessage(numStr.toInt(),"尝试连接到 $currentUrl")
                val url = URL(currentUrl)
                val conn = url.openConnection() as HttpURLConnection
                messageCenter.sendCurrentMessage(2,"等待回应")
                conn.doInput = true
                conn.doOutput = true
                conn.requestMethod = "GET"
                conn.readTimeout = 5000
                conn.connectTimeout = 8000
                try {
                    if (conn.responseCode == 200){//请求成功
                        messageCenter.sendCurrentMessage(3,"读取回应")
                        val htmlString = conn.inputStream.reader().readText()//网页源代码
                        //匹配标题
                        messageCenter.sendCurrentMessage(4,"解析标题")
                        var title = "Unknow"
                        val titleMatcher = titleCompiler.matcher(htmlString)
                        if (titleMatcher.find()){
                            val titleTemp = titleMatcher.group(0)
                            title = titleTemp.substring(4,titleTemp.length-8)
                        }

                        //关键字
                        messageCenter.sendCurrentMessage(5,"解析关键字")
                        var keyWord = "Unknow"
                        val keywordMatcher = keyWordCompiler.matcher(htmlString)
                        if (keywordMatcher.find()){
                            val keywordTemp = keywordMatcher.group(0)
                            keyWord = keywordTemp.substring(15,keywordTemp.length-20)
                        }
                        //介绍
                        messageCenter.sendCurrentMessage(6,"解析介绍")
                        var summary = "UnKnow"
                        val summaryMatcher = summaryCompiler.matcher(htmlString)
                        if (summaryMatcher.find()){
                            val summaryTemp = summaryMatcher.group(0)
                            summary = summaryTemp.substring(26,summaryTemp.length-44)
                        }
                        //折扣
                        messageCenter.sendCurrentMessage(7,"解析折扣")
                        var discount = "None"
                        val discountMatcher = discountCompiler.matcher(htmlString)
                        if (discountMatcher.find()){
                            val discountTemp = discountMatcher.group(0)
                            discount = discountTemp.substring(23,discountTemp.length-32)
                        }
                        //售价
                        messageCenter.sendCurrentMessage(8,"解析售价")
                        var salePrice = "-"
                        val salePriceMatcher = salePriceCompiler.matcher(htmlString)
                        if (salePriceMatcher.find()){
                            val salePriceTemp = salePriceMatcher.group(0)
                            salePrice = salePriceTemp.substring(25,salePriceTemp.length-32)
                        }
                        //原价
                        messageCenter.sendCurrentMessage(9,"解析原价")
                        var listPrice = "-"
                        val listPriceMatcher = listPriceCompiler.matcher(htmlString)
                        if (listPriceMatcher.find()){
                            val listPriceTemp = listPriceMatcher.group(0)
                            listPrice = listPriceTemp.substring(25,listPriceTemp.length-13)
                        }
                        //是否要Steam
                        messageCenter.sendCurrentMessage(10,"解析Steam关系")
                        var needSteam = false
                        val needSteamMatcher = needSteamCompiler.matcher(htmlString)
                        if (needSteamMatcher.find()){
                            needSteam = true
                        }
                        //标题图片
                        messageCenter.sendCurrentMessage(11,"解析图片地址")
                        var titleImage = "-"
                        val titleImageMatcher = titleImageCompiler.matcher(htmlString)
                        if (titleImageMatcher.find()){
                            val titleImageTemp = titleImageMatcher.group(0)
                            titleImage = titleImageTemp.substring(23,titleImageTemp.length-16)
                        }
                        //视频地址
                        messageCenter.sendCurrentMessage(12,"解析视频地址")
                        var previewVideo = "-"
                        val previewVideoMatcher = previewVideoCompiler.matcher(htmlString)
                        if (previewVideoMatcher.find()){
                            val previewVideoTemp = previewVideoMatcher.group(0)
                            previewVideo = previewVideoTemp.substring(13,previewVideoTemp.length-42)
                        }
                        messageCenter.sendMessage("$numStr 请求成功 标题:$title  售价:$salePrice  折扣:$discount  原价:$listPrice  是否需要Steam:$needSteam  简介:$summary")
                        messageCenter.sendCurrentMessage(13,"插入到数据库")
                        messageCenter.sendToDatabase(title,salePrice,listPrice,discount,needSteam,titleImage,previewVideo,summary,keyWord)
                    }else{//请求失败
                        urlCenter.reportBadLink(currentUrl)
                        messageCenter.sendMessage("$numStr 请求失败，可能此页面不存在或者请求失败")
                    }
                } catch(e: Exception) {
                    urlCenter.reportBadLink(currentUrl)
                    messageCenter.sendMessage("$numStr 请求超时")
                }
                messageCenter.sendCurrentMessage(14,"解析完毕")
            }
            messageCenter.sendMessage("爬虫结束")
            messageCenter.sendTotalMessage("解析完成")
        }

    }
}