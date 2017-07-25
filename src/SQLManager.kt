import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

/**
 * Created by CreeperSan on 2017/7/24.
 */

class SQLManager{
    private var conn:Connection
    private var statement:Statement

    init {
        conn = DriverManager.getConnection("jdbc:sqlite:杉果数据库_${getStrTime((System.currentTimeMillis()/1000).toString())}.db")
        conn.autoCommit = false
        statement = conn.createStatement()
        statement.executeUpdate("create table if not exists game(title text,sell text,price text,discount text,steam text,img text,video text,summary text,keyword text)")
    }

    fun insert(title:String,sell:String,price:String,discount:String,steam:Boolean,img:String,video:String,summary:String,keyword:String){

        try {
            statement.executeUpdate("insert into game (title,sell,price,discount,steam,img,video,summary,keyword) values (" +
                    "\"$title\",\"$sell\",\"$price\",\"$discount\",\"${steam}\",\"$img\",\"$video\",\"$summary\",\"$keyword\")")
            conn.commit()
        }catch (e:Exception){
            e.printStackTrace()
            print("-------------------------------------------")
        }
    }

    fun newOne(name:String){
        statement.close()
        conn.close()
        conn = DriverManager.getConnection("jdbc:sqlite:${name})}.db")
        conn.autoCommit = false
        statement = conn.createStatement()
        statement.executeUpdate("create table if not exists game(title text,sell text,price text,discount text,steam text,img text,video text,summary text,keyword text)")
    }

}
