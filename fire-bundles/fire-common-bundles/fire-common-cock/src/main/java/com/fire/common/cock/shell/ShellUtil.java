package com.fire.common.cock.shell;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class ShellUtil {

	public static boolean execShell(String[] command) {
		// TODO 参数检查
		InputStreamReader stdISR = null;
		InputStreamReader errISR = null;
		Process process = null;
		long timeout = 10 * 1000;
		try {
			// TODO command如果带参数，需要处理
			process = Runtime.getRuntime().exec(command);

			CommandStreamGobbler errorGobbler = new CommandStreamGobbler(process.getErrorStream(), command[0], "ERR");
			CommandStreamGobbler outputGobbler = new CommandStreamGobbler(process.getInputStream(), command[0], "STD");

			errorGobbler.start();
			// 必须先等待错误输出ready再建立标准输出
			while (!errorGobbler.isReady()) {
				Thread.sleep(10);
			}
			outputGobbler.start();
			while (!outputGobbler.isReady()) {
				Thread.sleep(10);
			}

			CommandWaitForThread commandThread = new CommandWaitForThread(process);
			commandThread.start();

			long commandTime = new Date().getTime();
			long nowTime = new Date().getTime();
			boolean timeoutFlag = false;
			while (!commandThread.isFinish()) {
				if (nowTime - commandTime > timeout) {
					timeoutFlag = true;
					break;
				} else {
					Thread.sleep(1000);
					nowTime = new Date().getTime();
				}
			}
			if (timeoutFlag) {
				// 命令超时
				errorGobbler.setTimeout(1);
				outputGobbler.setTimeout(1);
				System.out.println("正式执行命令：" + command + "超时");
			}

			while (true) {
				if (errorGobbler.isReadFinish() && outputGobbler.isReadFinish()) {
					break;
				}
				Thread.sleep(10);
			}
			return true;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (process != null) {
				process.destroy();
			}
			try {
				if (stdISR != null) {
					stdISR.close();
				}
				if(errISR != null) {
					errISR.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}