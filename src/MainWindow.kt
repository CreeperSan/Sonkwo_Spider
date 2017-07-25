import Compoment.JNumberField
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class MainWindow:JFrame(){
    //组件
    private var spiderController:SpiderController
    private var sqlManager = SQLManager()
    //控件
    private val startCountTextField:JNumberField
    private val endCountTextField:JNumberField
    private val startButton:JButton
    private val pauseButton:JButton
    private val stopButton:JButton
    private val currentLabel:JLabel
    private val currentProgress:JProgressBar
    private val totalLabel:JLabel
    private val totalProgress:JProgressBar
    private val processInfoList:JList<String>
    private val processInfoScrollPanel:JScrollPane
    private val defaultListModel:DefaultListModel<String>
    private val countLabel:JLabel

    init {
        setDefaultLookAndFeelDecorated(true)
        defaultCloseOperation = EXIT_ON_CLOSE
        //往下为布局
        val flowLayout = FlowLayout()
        flowLayout.alignment = FlowLayout.LEFT
        layout = flowLayout
        //添加功能按钮
        val functionPanel = JPanel()
        functionPanel.layout = FlowLayout(FlowLayout.LEADING)
        functionPanel.preferredSize = Dimension(120,800)
        val startCountLabel = JLabel("开始的序号")
        val endCountLabel = JLabel("结束的序号")
        startCountTextField = JNumberField()
        startCountTextField.preferredSize = Dimension(100,32)
        endCountTextField = JNumberField()
        endCountTextField.preferredSize = Dimension(100,32)
        startButton = JButton("开始")
        startButton.preferredSize = Dimension(100,32)
        pauseButton = JButton("暂停")
        pauseButton.preferredSize = Dimension(100,32)
        stopButton = JButton("停止")
        stopButton.preferredSize = Dimension(100,32)
        functionPanel.add(startCountLabel)
        functionPanel.add(startCountTextField)
        functionPanel.add(endCountLabel)
        functionPanel.add(endCountTextField)
        functionPanel.add(startButton)
        functionPanel.add(pauseButton)
        functionPanel.add(stopButton)
        //添加右侧信息展示
        val detailPanel = JPanel()
        detailPanel.preferredSize = Dimension(1142,800)
        currentLabel = JLabel("当前没有任务正在运行")
        currentLabel.preferredSize = Dimension(1130,32)
        currentProgress = JProgressBar()
        currentProgress.minimum = 0
        currentProgress.maximum = 14
        currentProgress.preferredSize = Dimension(1130,16)
        totalLabel = JLabel("当前没有任务正在运行")
        totalLabel.preferredSize = Dimension(1130,32)
        totalProgress = JProgressBar()
        totalProgress.preferredSize = Dimension(1130,16)
        countLabel = JLabel("还不稳定，慎用")
        countLabel.preferredSize = Dimension(1130,32)
        processInfoList = JList<String>()
        processInfoScrollPanel = JScrollPane()
        processInfoScrollPanel.setViewportView(processInfoList)
        processInfoScrollPanel.preferredSize = Dimension(1130,510)//滚动
        defaultListModel = DefaultListModel<String>()
        processInfoList.model = defaultListModel

        detailPanel.add(currentLabel)
        detailPanel.add(currentProgress)
        detailPanel.add(totalLabel)
        detailPanel.add(totalProgress)
        detailPanel.add(countLabel)
        detailPanel.add(processInfoScrollPanel)
        //添加到布局
        add(functionPanel)
        add(detailPanel)

        //动作
        spiderController = SpiderController(this)
        startButton.addActionListener {
            val startCount = startCountTextField.text.toInt()
            val endCount = endCountTextField.text.toInt()
            totalProgress.minimum = startCount
            totalProgress.maximum = endCount
            spiderController.start(startCount,endCount)
        }
    }

    /**
     *      统一的调用
     */
    fun addMessage(message:String){
        defaultListModel.addElement(message)
//        println(message)
        processInfoScrollPanel.verticalScrollBar.setValue(processInfoList.indexToLocation(defaultListModel.size()-1).y)
    }

    fun addCurrentMessage(progress:Int,message: String){
        currentProgress.value = progress
        currentLabel.text = message
    }

    fun addTotalMessage(progress: Int,message: String){
        totalProgress.value = progress
        totalLabel.text = message
    }

    fun addTotalMessage(message: String){
        totalLabel.text = message
    }

    fun insertIntoDatabase(title:String,sell:String,price:String,discount:String,steam:Boolean,img:String,video:String,summary:String,keyword:String){
        sqlManager.insert(title, sell, price, discount, steam, img, video, summary, keyword)
    }
}
