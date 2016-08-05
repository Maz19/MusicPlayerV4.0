package cn.tedu.music_player_v4.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;
import cn.tedu.music_player_v4.entity.Music;

/**
 * 解析xml的工具类
 */
public class XmlParser {
	/**
	 * 解析输入流  获取List<Music>
	 * @param is
	 * @return
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	public static List<Music> parseMusicList(InputStream is) throws IOException, XmlPullParserException{
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");
		int eventType=parser.getEventType();
		List<Music> musics = new ArrayList<Music>();
		Music music = null;
		while(eventType != XmlPullParser.END_DOCUMENT){
			switch (eventType) {
			case XmlPullParser.START_TAG:
				String name=parser.getName();
				if("song".equals(name)){
					music = new Music();
					musics.add(music);
				}else if("artist_id".equals(name)){
					music.setArtist_id(parser.nextText());
				}else if("language".equals(name)){
					music.setLanguage(parser.nextText());
				}else if("pic_big".equals(name)){
					music.setPic_big(parser.nextText());
				}else if("pic_small".equals(name)){
					music.setPic_small(parser.nextText());
				}else if("publishtime".equals(name)){
					music.setPublishtime(parser.nextText());
				}else if("lrclink".equals(name)){
					music.setLrclink(parser.nextText());
				}else if("all_artist_ting_uid".equals(name)){
					music.setAll_artist_ting_uid(parser.nextText());
				}else if("all_artist_id".equals(name)){
					music.setAll_artist_id(parser.nextText());
				}else if("style".equals(name)){
					music.setStyle(parser.nextText());
				}else if("song_id".equals(name)){
					music.setSong_id(parser.nextText());
				}else if("title".equals(name)){
					music.setTitle(parser.nextText());
				}else if("author".equals(name)){
					music.setAuthor(parser.nextText());
				}else if("album_id".equals(name)){
					music.setAlbum_id(parser.nextText());
				}else if("album_title".equals(name)){
					music.setAlbum_title(parser.nextText());
				}else if("artist_name".equals(name)){
					music.setArtist_name(parser.nextText());
				}
				break;
			}
			//向后驱动事件
			eventType=parser.next();
		}
		return musics;
	}
	
}


