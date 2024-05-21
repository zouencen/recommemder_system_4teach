package 软件工程团队作业;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class recommender_online {
	public static final String PATH = "C:/Users/Administrator/Desktop/recommender_system_data/";
	public static String sim_path = PATH + "sim.txt";
	public static String movies_path = PATH + "movies.dat";
	public static HashMap<String, HashMap<String, Float>> sim = new HashMap<String, HashMap<String, Float>>();

	public static void main(String[] args) throws IOException {
		MappedBiggerFileReader reader = new MappedBiggerFileReader(sim_path, 65536);
		String temp = "";
		int k = 0;
		while (reader.read() != -1) {
			String txt = temp + new String(reader.getArray());
			String[] line = txt.split("\r\n");
			temp = line[line.length - 1];
			for (int i = 0; i < line.length - 1; i++) {
				String[] param = line[i].split("::");
				if (sim.get(param[0]) == null)
					sim.put(param[0], new HashMap<String, Float>());
				sim.get(param[0]).put(param[1], Float.parseFloat(param[2]));
			}
			line = null;
		}
		String[] param = temp.split("::");
		if (sim.get(param[0]) == null)
			sim.put(param[0], new HashMap<String, Float>());
		sim.get(param[0]).put(param[1], Float.parseFloat(param[2]));
		reader.close();
		HashMap<String, String> movies_map = new HashMap<String, String>();
		String txt = FileUtil.read(movies_path);
		String[] moives = txt.split("\n");
		for (String m : moives) {
			String[] one = m.split("::");
			movies_map.put(one[0], one[1] + ": " + one[2]);
		}

		while (true) {
			System.out.println("请输入电影编号：");
			String movieID = new Scanner(System.in).next();
			System.out.println("你输入的电影信息：");
			if (movies_map.get(movieID) != null) {
				System.out.println(movieID + " - " + movies_map.get(movieID));
			} else {
				System.out.println("影片不存在！");
				continue;
			}

			HashMap<String, Float> map = sim.get(movieID);
			List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(map.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
				// 降序
				public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});
			System.out.println("推荐影片如下：");
			int i = 0;
			for (Map.Entry<String, Float> mapping : list) {
				if (i >= 15)
					break;
				System.out.println(mapping.getKey() + " - " + movies_map.get(mapping.getKey()));
				i++;

			}
		}

	}

}
