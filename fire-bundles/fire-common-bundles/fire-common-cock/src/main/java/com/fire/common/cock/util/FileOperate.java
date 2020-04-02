package com.fire.common.cock.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileOperate {
	private static final Logger LOG = LoggerFactory.getLogger(FileOperate.class);

	// private static final String FILE_PATH =
	// "E:/git/wolf/wolf-bundles/core-bundles/core-sensitive/src/main/resource/lexicon/敏感词/敏感词库大全.txt";
	//
	// private static final String DIR_PATH =
	// "E:/git/wolf/wolf-bundles/core-bundles/core-sensitive/src/main/resource/lexicon";

	/**
	 * 以字节为单位读取文件内容
	 * 
	 * @param filePath
	 *            ：需要读取的文件路径
	 */
	public static void readFileByByte(String filePath) {
		File file = new File(filePath);
		// InputStream:此抽象类是表示字节输入流的所有类的超类。
		InputStream ins = null;
		try {
			// FileInputStream:从文件系统中的某个文件中获得输入字节。
			ins = new FileInputStream(file);
			int temp;
			// read():从输入流中读取数据的下一个字节。
			while ((temp = ins.read()) != -1) {
				System.out.write(temp);
			}
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.getStackTrace();
				}
			}
		}
	}

	/**
	 * 以字符为单位读取文件内容
	 * 
	 * @param filePath
	 */
	public static void readFileByCharacter(String filePath) {
		File file = new File(filePath);
		// FileReader:用来读取字符文件的便捷类。
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			int temp;
			while ((temp = reader.read()) != -1) {
				if (((char) temp) != '\r') {
					System.out.print((char) temp);
				}
			}
		} catch (IOException e) {
			e.getStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 以行为单位读取文件内容
	 * 
	 * @param filePath
	 */
	public static void readFileByLine(String filePath) {
		File file = new File(filePath);
		// BufferedReader:从字符输入流中读取文本，缓冲各个字符，从而实现字符、数组和行的高效读取。
		BufferedReader buf = null;
		try {
			// FileReader:用来读取字符文件的便捷类。
			buf = new BufferedReader(new FileReader(file));
			// buf = new BufferedReader(new InputStreamReader(new
			// FileInputStream(file)));
			String temp = null;
			while ((temp = buf.readLine()) != null) {
				System.out.println(temp);
			}
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			if (buf != null) {
				try {
					buf.close();
				} catch (IOException e) {
					e.getStackTrace();
				}
			}
		}
	}

	public static Set<String> readNioFilePath(String filePath) {
		Set<String> strings = new HashSet<String>();
		List<File> list = FileUtil.filesByDirectory(new File(filePath), new ArrayList<File>());
		list.stream().forEach(t -> {
			try {
				CharBuffer cb = readNioFile(t);

				String str = System.getProperty("line.separator");

				char[] chars = cb.array();
				int start = 0;
				for (int i = start; i < chars.length; i++) {
					if (chars[i] == str.charAt(0) || (str.length() > 1 && chars[i] == str.charAt(1))) {
						strings.add(String.copyValueOf(chars, start, i - start));
						start = i;
						if (chars[i] == str.charAt(str.length() > 1 ? 1 : 0)) {
							start++;
						}
					}
				}
				cb.clear();

			} catch (IOException e) {
				LOG.warn("file {} read error : {}", t, e);
			}

		});
		return strings;
	}

	/**
	 * 使用Java.nio ByteBuffer字节将一个文件输出至另一文件
	 * 
	 * @param filePath
	 */
	public static CharBuffer readNioFile(File file) throws IOException {
		RandomAccessFile in = null;
		FileChannel fcIn = null;
		MappedByteBuffer buffer = null;
		try {
			// 获取源文件和目标文件的输入输出流
			in = new RandomAccessFile(file, "r");
			// 获取输入输出通道
			fcIn = in.getChannel();
			buffer = fcIn.map(MapMode.READ_ONLY, 0, fcIn.size());

			Charset charset = Charset.forName("UTF-8");
			CharsetDecoder decoder = charset.newDecoder();
			CharBuffer charBuffer = decoder.decode(buffer);
			// flip方法让缓冲区可以将新读入的数据写入另一个通道
			buffer.flip();
			return charBuffer;
		} catch (IOException e) {
			try {
				Charset charset = Charset.forName(System.getProperty("file.encoding"));
				CharsetDecoder decoder = charset.newDecoder();
				CharBuffer charBuffer = decoder.decode(buffer);
				buffer.flip();
				return charBuffer;
			} catch (Exception e1) {
				try {
					Charset charset = Charset.forName("GBK");
					CharsetDecoder decoder = charset.newDecoder();
					CharBuffer charBuffer = decoder.decode(buffer);
					buffer.flip();
					return charBuffer;
				} catch (Exception e2) {
					try {
						Charset charset = Charset.forName("ISO8859-1");
						CharsetDecoder decoder = charset.newDecoder();
						CharBuffer charBuffer = decoder.decode(buffer);
						buffer.flip();
						return charBuffer;
					} catch (IOException e3) {
						Charset charset = Charset.defaultCharset();
						CharsetDecoder decoder = charset.newDecoder();
						CharBuffer charBuffer = decoder.decode(buffer);
						buffer.flip();
						return charBuffer;
					}
				}
			}

		} finally {
			try {
				if (fcIn != null) {
					fcIn.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				LOG.warn("file {} close error : {}", file, e);
				throw e;
			}
			if (buffer != null) {
				buffer.clear();
				unmap(buffer);
			}
		}
	}

	public static void unmap(final MappedByteBuffer mappedByteBuffer) {
		if (mappedByteBuffer == null) {
			return;
		}
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				try {
					Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
					if (getCleanerMethod != null) {
						getCleanerMethod.setAccessible(true);
						Object cleaner = getCleanerMethod.invoke(mappedByteBuffer, new Object[0]);
						Method cleanMethod = cleaner.getClass().getMethod("clean", new Class[0]);
						if (cleanMethod != null) {
							cleanMethod.invoke(cleaner, new Object[0]);
						}
					}
				} catch (Exception e) {
					FileOperate.LOG.error("{}", e);
				}
				return null;
			}

		});
	}

	public static void main(String args[]) {
		// long start = System.currentTimeMillis();
		// readFileByByte(FILE_PATH);
		// readFileByCharacter(FILE_PATH);
		// readFileByLine(FILE_PATH);
		// System.out.println("----");
		// System.out.println(System.currentTimeMillis() - start);
		//
		// Set<String> strings = readNioFilePath(DIR_PATH);
		// System.out.println(strings);

		System.out.println(System.getProperty("file.encoding"));

	}
}
