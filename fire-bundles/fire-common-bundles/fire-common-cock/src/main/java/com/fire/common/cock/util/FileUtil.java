package com.fire.common.cock.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class FileUtil {
	public static List<String> scanFilePaths(String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		List<String> list = new ArrayList<String>();
		if (files != null) {
			for (File f : files) {
				list.add(f.getPath());
			}
		}
		return list;
	}

	public static File[] scanFiles(String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		return files;
	}

	public static List<File> filesByDirectory(File file, List<File> list) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isFile())
					list.add(f);
				if (f.isDirectory())
					filesByDirectory(f, list);
			}
		}
		return list;
	}

	public static void main(String[] args) {
		List<File> list = filesByDirectory(
				new File(
						"E:/git/wolf/wolf-bundles/core-bundles/core-sensitive/src/main/resource/lexicon"),
				new ArrayList<File>());
		System.out.print(list);
	}

	public static boolean delete(String f) {
		try {
			File file = new File(f);
			file.delete();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String read(String file) {
		String ret = null;

		File f = null;
		BufferedInputStream result = null;
		ByteArrayOutputStream baos = null;
		try {
			f = new File(file);
			// String str1;
			if (!f.exists())
				return ret;
			if (!f.isFile()) {
				return ret;
			}
			result = new BufferedInputStream(new FileInputStream(f));
			baos = new ByteArrayOutputStream();
			byte[] cont = new byte[1024];
			int conlen;
			while ((conlen = result.read(cont)) >= 0) {
				// int conlen;
				baos.write(cont, 0, conlen);
			}
			ret = new String(baos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (result != null)
					result.close();
				if (baos != null)
					baos.close();
				f = null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (result != null)
					result.close();
				if (baos != null)
					baos.close();
				f = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static byte[] readBytes(String file) {
		byte[] ret = null;

		File f = null;
		BufferedInputStream result = null;
		ByteArrayOutputStream baos = null;
		try {
			f = new File(file);
			// byte[] arrayOfByte1;
			if (!f.exists())
				return ret;
			if (!f.isFile()) {
				return ret;
			}
			result = new BufferedInputStream(new FileInputStream(f));
			baos = new ByteArrayOutputStream();
			byte[] cont = new byte[1024];
			int conlen;
			while ((conlen = result.read(cont)) >= 0) {
				// int conlen;
				baos.write(cont, 0, conlen);
			}
			ret = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (result != null)
					result.close();
				if (baos != null)
					baos.close();
				f = null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (result != null)
					result.close();
				if (baos != null)
					baos.close();
				f = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static boolean write(String content, String file) {
		try {
			return write(content, file, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean write(byte[] content, String file) {
		boolean ret = false;

		FileOutputStream fos = null;
		try {
			File filedir = new File(getPath(file));
			if (!filedir.exists())
				filedir.mkdirs();
			fos = new FileOutputStream(file);
			fos.write(content);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (fos != null)
					fos.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static boolean write(String content, String file, boolean append) {
		boolean ret = false;
		FileOutputStream fos = null;
		try {
			long t1 = System.currentTimeMillis();
			File filedir = new File(getPath(file));
			if (!filedir.exists()) {
				filedir.mkdirs();
			}
			if (append)
				fos = new FileOutputStream(file, append);
			else {
				fos = new FileOutputStream(file);
			}
			fos.write(content.getBytes());
			long t2 = System.currentTimeMillis();
			System.out.println(t2 - t1);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static String getPath(String f) {
		try {
			return f.substring(0, f.lastIndexOf("/"));
		} catch (Exception e) {
		}
		return "./";
	}

	public static String[] getFileList(String dir) {
		try {
			File parent = new File(dir);
			if ((!parent.isAbsolute()) || (!parent.isDirectory())) {
				return null;
			}
			return parent.list();
		} catch (Exception e) {
		}
		return null;
	}

	public static String[] getFileList(String dir, String pattern) {
		try {
			File parent = new File(dir);
			if ((!parent.isAbsolute()) || (!parent.isDirectory())) {
				return null;
			}
			// Pattern namePattern = Pattern.compile(pattern);
			// return parent.list(new FilenameFilter(namePattern) {
			// public boolean accept(File dir, String name) {
			// return FileUtil.this.matcher(name).matches();
			// }
			//
			// });
			return parent.list();
		} catch (Throwable te) {
			te.printStackTrace();
		}
		return null;
	}

	public static InputStream getStringToStream(String sInputString) {
		if ((sInputString != null) && (!sInputString.trim().equals(""))) {
			try {
				ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(
						sInputString.getBytes());
				return tInputStringStream;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public static String getStreamToString(InputStream tInputStream) {
		if (tInputStream != null) {
			try {
				BufferedReader tBufferedReader = new BufferedReader(
						new InputStreamReader(tInputStream));
				StringBuffer tStringBuffer = new StringBuffer();
				String sTempOneLine = new String("");
				while ((sTempOneLine = tBufferedReader.readLine()) != null) {
					tStringBuffer.append(sTempOneLine);
				}
				return tStringBuffer.toString();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public static boolean writeProperties(String str, String path) {
		try {
			Properties properties = new Properties();
			InputStream is = getStringToStream(str);
			properties.load(is);
			OutputStream file = new FileOutputStream(path);
			properties.store(file, "gererateProperties");
			file.close();
			return true;
		} catch (Exception e) {
		}
		throw new RuntimeException("The specified path was not found");
	}

	public static String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}