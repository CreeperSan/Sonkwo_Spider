package Compoment

import java.awt.event.*
import javax.swing.JTextField

class JNumberField:JTextField(){

    init {
        addKeyListener(NumberOnlyKeyAdapter.keyAdapter)
    }

    object NumberOnlyKeyAdapter{
        val keyAdapter = object : KeyAdapter(){
            override fun keyTyped(e: KeyEvent?) {
                val keyChar = e!!.keyChar.toInt()
                if (keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) {
                    super.keyTyped(e)
                } else {
                    e.consume() //关键，屏蔽掉非法输入
                }
            }
        }
    }
}
