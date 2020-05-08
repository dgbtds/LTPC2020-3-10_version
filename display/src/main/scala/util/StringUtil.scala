package scala.util

/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/4/20 13:18
 */
object StringUtil {
	//byte 数组与 int 的相互转换
	def byteArrayToInt(b: Array[Byte]): Int = b(3) & 0xFF | (b(2) & 0xFF) << 8 | (b(1) & 0xFF) << 16 | (b(0) & 0xFF) << 24
	def intToByteArray(a: Int): Array[Byte] = Array[Byte](((a >> 24) & 0xFF).toByte, ((a >> 16) & 0xFF).toByte, ((a >> 8) & 0xFF).toByte, (a & 0xFF).toByte)
}
