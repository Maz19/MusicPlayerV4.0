package cn.tedu.music_player_v4.util;

import android.util.Log;
import cn.tedu.music_player_v4.entity.LrcLine;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * �йظ�ʵĹ�����
 */
public class LrcUtils {
	
	public static List<LrcLine> parseLrc(File targetFile) throws IOException{
		if(!targetFile.exists()){
			return null;
		}
		FileInputStream is = new FileInputStream(targetFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = "";
		List<LrcLine> lines = new ArrayList<LrcLine>();
		while((line=reader.readLine()) != null){
			if("".equals(line)){
				continue;
			}
			if(line.indexOf("[")<0){ //û��ʱ��
				continue;
			}
			Log.i("info", "������ݣ�"+line);
			String time=line.substring(1, line.indexOf("]"));
			String content=line.substring(line.indexOf("]")+1);
			LrcLine l = new LrcLine(time, content);
			lines.add(l);
		}
		return lines;
	}
	
	/**
	 * �������   �����ڽ����Ĺ����аѸ�ʴ����ļ�
	 * @param is
	 * @param targetFile
	 * @return
	 * @throws IOException 
	 */
	public static List<LrcLine> parseLrc(InputStream is, File targetFile) throws IOException{
		//���ж�ȡ������
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		List<LrcLine> lines = new ArrayList<LrcLine>();
		//��ȡ�Ĺ��̵��а��ı����뻺���ļ�
		PrintWriter out = new PrintWriter(targetFile);
		while( (line=reader.readLine()) != null){
			//line ������:   
			//line ������:   [00:04.52]������G.E.M. ������
			//line ������:   [ti:��]
			//line ������:   ʲôʲô��
			if("".equals(line)){
				continue;
			}
			if(line.indexOf("[")<0){ //û��ʱ��
				continue;
			}
			//��line�е����� д�������
			out.println(line);
			out.flush();
			Log.i("info", "������ݣ�"+line);
			String time=line.substring(1, line.indexOf("]"));
			String content=line.substring(line.indexOf("]")+1);
			LrcLine l = new LrcLine(time, content);
			lines.add(l);
		}
		out.close();
		return lines;
	}
	
}
