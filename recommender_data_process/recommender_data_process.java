package 软件工程团队作业;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class recommender_data_process {

	public static final String PATH = "C:/Users/Administrator/Desktop/recommender_system_data/";
	// public static String moviesPath =
	// "C:/Users/Administrator/Desktop/recommender_system_data/movies.dat";
	public static String ratingsPath = PATH + "ratings_train.dat";
	public static String item_to_user_path = PATH + "item_to_user.txt";
	public static String user_to_item_path = PATH + "user_to_item.txt";
	public static String neighbors_path = PATH + "neighbors.txt";
	// public static String neighbors_path1 = PATH + "neighbors1.txt";
	public static String sim_path = PATH + "sim.txt";

	public static HashMap<String, ArrayList<String>> item_to_user = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, ArrayList<String>> user_to_item = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, ArrayList<String>> neighbors = new HashMap<String, ArrayList<String>>();
	// public static HashMap<String, HashMap<String, String>> sim = new
	// HashMap<String, HashMap<String, String>>();

	public static void main(String[] args) throws IOException, InterruptedException {
		temp3();
	}

	public static void temp1() throws IOException {
		// String txt = FileUtil.read(ratingsPath);
		// txt = txt.replaceAll("\n", "::");
		// StringTokenizer st = new StringTokenizer(txt, "::");
		// int i = 0;
		// while (st.hasMoreTokens()) {
		// String s1 = st.nextToken().toString();
		// String s2 = st.nextToken().toString();
		// String s3 = st.nextToken().toString();
		//
		// addItem(item_to_user, s2, s1, s3);
		// addItem(user_to_item, s1, s2, s3);
		// st.nextToken();
		// i++;
		// if (i % 100000 == 0) {
		// System.out.println(i);
		// }
		// }
		//
		// writeToTxt(item_to_user,
		// "C:/Users/Administrator/Desktop/recommender_system_data/item_to_user.txt");
		// writeToTxt(user_to_item,
		// "C:/Users/Administrator/Desktop/recommender_system_data/user_to_item.txt");

		MappedBiggerFileReader reader = new MappedBiggerFileReader(ratingsPath, 65536);
		String temp = "";
		int k = 0;
		while (reader.read() != -1) {
			String txt = temp + new String(reader.getArray());
			String[] line = txt.split("\r\n");
			temp = line[line.length - 1];
			for (int i = 0; i < line.length - 1; i++) {
				String[] param = line[i].split("::");
				addItem(item_to_user, param[1], param[0], param[2]);
				addItem(user_to_item, param[0], param[1], param[2]);
				k++;
				if (k % 100000 == 0)
					System.out.println(k);
				param = null;
			}
			line = null;
		}
		String[] param = temp.split("::");
		addItem(item_to_user, param[1], param[0], param[2]);
		addItem(user_to_item, param[0], param[1], param[2]);
		reader.close();
		writeToTxt(item_to_user, item_to_user_path);
		writeToTxt(user_to_item, user_to_item_path);
	}

	public static void temp2() {
		item_to_user = (HashMap<String, ArrayList<String>>) readFromTxt(item_to_user_path);
		user_to_item = (HashMap<String, ArrayList<String>>) readFromTxt(user_to_item_path);

		for (Entry<String, ArrayList<String>> entry : item_to_user.entrySet()) {
			if (neighbors.get(entry.getKey()) == null)
				neighbors.put(entry.getKey(), new ArrayList<String>());
			// 遍历看过这个电影的用户列表
			for (int i = 0; i < entry.getValue().size(); i += 2) {
				String userID = entry.getValue().get(i);
				ArrayList<String> moives = user_to_item.get(userID);

				for (int j = 0; j < moives.size(); j += 2) {
					if (!entry.getKey().equals(moives.get(j)))
						neighbors.get(entry.getKey()).add(moives.get(j));
				}
				userID = null;
				moives = null;
			}
		}

		writeToTxt(neighbors, neighbors_path);
	}

	public static void temp3() {

		// HashMap<String, HashSet<String>> map = (HashMap<String,
		// HashSet<String>>) readFromTxt(neighbors_path);
		// System.out.println(1);
		// for (Entry<String, HashSet<String>> entry : map.entrySet()) {
		// neighbors.put(entry.getKey(), new
		// ArrayList<String>(entry.getValue()));
		// }
		// System.out.println(neighbors.size());
		// writeToTxt(neighbors, neighbors_path1);

		item_to_user = (HashMap<String, ArrayList<String>>) readFromTxt(item_to_user_path);
		HashMap<String, HashMap<String, Float>> map = new HashMap<String, HashMap<String, Float>>();

		for (Entry<String, ArrayList<String>> entry : item_to_user.entrySet()) {
			if (map.get(entry.getKey()) == null)
				map.put(entry.getKey(), new HashMap<String, Float>());
			for (int i = 0; i < entry.getValue().size(); i += 2) {
				map.get(entry.getKey()).put(entry.getValue().get(i), Float.parseFloat(entry.getValue().get(i + 1)));
			}
		}

		neighbors = (HashMap<String, ArrayList<String>>) readFromTxt(neighbors_path);
		System.out.println("数据加载完毕！");
		// 遍历邻居表
		System.out.println("neighbors_size:" + neighbors.size());
		int i = 0;
		ArrayList<Float> time = new ArrayList<Float>();
		for (Entry<String, ArrayList<String>> entry : neighbors.entrySet()) {
			long start = System.currentTimeMillis();
			// 一部影片的用户评分
			HashMap<String, Float> pf1 = map.get(entry.getKey());
			// 遍历邻居影片
			StringBuilder sb = new StringBuilder();
			for (String s : entry.getValue()) {
				// 邻居影片的用户评分
				HashMap<String, Float> pf2 = map.get(s);
				sb.append(entry.getKey());
				sb.append("::");
				sb.append(s);
				sb.append("::");
				sb.append(getSim(pf1, pf2));
				sb.append("\r\n");

				// // 首次计算邻居评分
				// if (sim.get(entry.getKey()) == null)
				// sim.put(entry.getKey(), new HashMap<String, String>());
				//
				// // 假如评分不存在 说明这两个影片之间的关系没有计算过 则计算
				// // 计算后同时给两个影片的相似度都赋值
				// if (sim.get(entry.getKey()).get(s) == null) {
				// float pf = getSim(pf1, pf2);
				// sim.get(entry.getKey()).put(s, pf + "");
				//
				// // 如果邻居影片没有计算过相似度 则新建空间
				// if (sim.get(s) == null)
				// sim.put(s, new HashMap<String, String>());
				// sim.get(s).put(entry.getKey(), pf + "");
				// }
			}
			FileUtil.write(sim_path, true, sb.toString());

			long end = System.currentTimeMillis();
			time.add((float) (end - start) / 1000);
			float t = 0;
			for (float f : time) {
				t += f;
			}
			t = t / time.size();
			t = t * neighbors.size() - i;// 剩余总时间 秒

			i++;
			System.out.println("进度：" + i + "/" + neighbors.size() + ",预计剩余时间：" + (int) t / 60 / 60 + "小时"
					+ (int) t / 60 % 60 + "分钟" + (int) t % 60 + "秒");
		}
		// writeToTxt(sim, sim_path);
	}

	public static float getSim(HashMap<String, Float> pf1, HashMap<String, Float> pf2) {
		ArrayList<Float> a1 = new ArrayList<Float>();
		ArrayList<Float> a2 = new ArrayList<Float>();

		HashSet<String> set = new HashSet<String>();
		set.addAll(pf1.keySet());
		set.addAll(pf2.keySet());

		for (Entry<String, Float> entry : pf1.entrySet()) {
			a1.add(entry.getValue());
			a2.add(getNeighborsPF(pf2, entry.getKey()));
			set.remove(entry.getKey());
		}

		for (String s : set) {
			a1.add((float) 0);
			a2.add(getNeighborsPF(pf2, s));
		}

		return sim(a1, a2);
	}

	public static float getNeighborsPF(HashMap<String, Float> pf, String userID) {
		if (pf.containsKey(userID))
			return pf.get(userID);
		return 0;
	}

	public static void addItem(HashMap<String, ArrayList<String>> item, String key, String param1, String param2) {
		if (item.get(key) == null) {
			item.put(key, new ArrayList<String>());
		}

		item.get(key).add(param1);
		item.get(key).add(param2);

	}

	public static void writeToTxt(Object map, String path) {
		// StringBuilder str = new StringBuilder();
		// int k = 0;
		// for (String s : map.keySet()) {
		// str.append(s);
		// str.append("::");
		// for (int i = 0; i < map.get(s).size(); i++) {
		// str.append(map.get(s).get(i));
		// str.append(",");
		// }
		// str.replace(str.length() - 1, str.length(), "&&");
		// k++;
		// System.out.println(k);
		// FileUtil.write(path, true, str.toString());
		// str.setLength(0);
		// }
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Object readFromTxt(String path) {
		// String txt = FileUtil.read(path);
		// String[] list = txt.split("&&");
		// for (int i = 0; i < list.length - 1; i++) {
		// if (list[i].equals(""))
		// continue;
		// String[] split = list[i].split("::");
		// if (result.get(split[0]) == null) {
		// result.put(split[0], new ArrayList<String>());
		// }
		// for (String s : split[1].split(",")) {
		// result.get(split[0]).add(s);
		// }
		// }

		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(path));
			return objectInputStream.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static float sim(ArrayList<Float> va, ArrayList<Float> vb) {
		// 如果向量维度不相等，则不能计算，函数退出
		if (va.size() != vb.size()) {
			return 0;
		}

		float num = 0;// numerator分子
		float den = 0;// denominator分母
		float den1 = 0;
		float den2 = 0;

		for (int i = 0; i < va.size(); i++) {
			num += va.get(i) * vb.get(i);
			den1 += Math.pow(va.get(i), 2);
			den2 += Math.pow(vb.get(i), 2);
		}
		den = (float) (Math.sqrt(den1) * Math.sqrt(den2));

		return num / den;
	}
}
