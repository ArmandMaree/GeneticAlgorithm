import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by armandmaree on 2016/05/29.
 */
public class Gene {
	private String imageName;
	private int imageColor;

	public Gene(String imageName) {
		this.imageName = imageName;
		recalcColor();
	}

	public void recalcColor() {
		BufferedImage inputImage = null;

		try {
			inputImage = ImageIO.read(new File(imageName));
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("ImageName: " + imageName);
			System.exit(0);
		}
		catch (IllegalArgumentException iae) {
			ImageInputStream stream = null;

			try {
				stream = ImageIO.createImageInputStream(new FileInputStream(imageName));
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("ImageName: " + imageName);
				System.exit(0);
			}

			Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);

			Exception lastException = null;
			while (iter.hasNext()) {
				ImageReader reader = null;
				try {
					reader = iter.next();
					ImageReadParam param = reader.getDefaultReadParam();
					reader.setInput(stream, true, true);
					Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes(0);
					while (imageTypes.hasNext()) {
						ImageTypeSpecifier imageTypeSpecifier = imageTypes.next();
						int bufferedImageType = imageTypeSpecifier.getBufferedImageType();
						if (bufferedImageType == BufferedImage.TYPE_BYTE_GRAY) {
							param.setDestinationType(imageTypeSpecifier);
							break;
						}
					}
					inputImage = reader.read(0, param);
					if (null != inputImage) break;
				} catch (Exception e) {
					lastException = e;
				} finally {
					if (null != reader) reader.dispose();
				}
			}
			// If you don't have an image at the end of all readers
			if (null == inputImage) {
				if (null != lastException) {
					try {
						throw lastException;
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		}

		int numRead = 0;
		Raster raster = null;
		try {
			raster = inputImage.getRaster();
		}
		catch(NullPointerException npe) {
			System.out.println(imageName);
			System.exit(0);
		}
		int maxRow = raster.getHeight() - 1;
		int maxCol = raster.getWidth() - 1;

		for (int i = 0; i < maxRow; i++) {
			for (int j = 0; j < maxCol; j++) {
				imageColor = (imageColor * numRead++ + inputImage.getRGB(j, i)) / numRead;
			}
		}
	}

	public void setImage(String imageName) {
		this.imageName = imageName;
		recalcColor();
	}

	public String getImageName() {
		return imageName;
	}

	public int getImageColor() {
		return imageColor;
	}

	@Override
	public String toString() {
		return imageName;
	}
}
