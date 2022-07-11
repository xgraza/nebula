package wtf.nebula.util.io

import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO

object ResourceUtil {
    fun getResourceStream(location: String?): InputStream? {
        return ResourceUtil::class.java.getResourceAsStream(location)
    }

    fun getResourceAsImage(location: String?): BufferedImage? {
        val inputStream: InputStream = getResourceStream(location) ?: return null
        return try {
            val image: BufferedImage = ImageIO.read(inputStream)
            inputStream.close()
            image
        } catch (e: IOException) {
            null
        }
    }
}