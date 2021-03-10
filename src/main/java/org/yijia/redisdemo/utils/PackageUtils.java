package org.yijia.redisdemo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PackageUtils {
    /**
     * 随机创建指定数量的红包数据
     *
     * @param money
     * @param count
     * @return
     */
    public static ArrayList<Integer> randDivide(Integer money, int count) {
        // 创建一个长度的红包数组
        ArrayList<Integer> redList = new ArrayList<>();

        // 由于double的精度分体将其转换为int计算, 即将元转换为分计算，红包最小单位以分计算
        int totalMoney = (int) (money * 100);

        // 判断红包的总金额
        if (money > 200) {
            System.out.println("单个红包不能超过200元");
            return redList; // 返回空的红包集合
        }
        if (totalMoney < count || totalMoney < 1) {
            System.out.println("被拆分的总金额不能小于0.01元");
            return redList; // 返回空的红包集合
        }
        //2. 进行随机分配
        Random rand = new Random();

        int leftMoney = totalMoney;
        int leftCount = count;
        // 随机分配公式：1 + rand.nextInt(leftMoney / leftCount * 2);
        for (int i = 0; i < count - 1; i++) {
            int money_ = 1 + rand.nextInt(leftMoney / leftCount * 2);
            redList.add(money_);
            leftMoney -= money_;
            leftCount--;
        }
        // 把剩余的最后一个放到最后一个包里
        redList.add(leftMoney);
        return redList;
    }

    /**
     * 平均创建指定数量的红包数据
     *
     * @param money
     * @param count
     * @return
     */
    public static ArrayList<Integer> averageDivide(Integer money, int count) {
        // 创建一个长度的红包数组
        ArrayList<Integer> redList = new ArrayList<>();

        // 由于double的精度分体将其转换为int计算, 即将元转换为分计算，红包最小单位以分计算
        int totalMoney = (int) (money * 100);

        int avg = totalMoney / count;
        int mod = totalMoney % count;

        for (int i = 0; i < count - 1; i++) {
            redList.add(avg);
        }
        redList.add(avg + mod);
        return redList;
    }

    public static String[] transStringArray(List<?> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = "" + list.get(i);
        }
        return array;
    }

}
