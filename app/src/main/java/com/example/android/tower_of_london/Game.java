package com.example.android.tower_of_london;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Game {
    /**
     * generate a randowm number
     */
    private static Random rand = new Random();
    /**
     * maximum number of items.
     */
    public static final int MAX_NUM = 12;
    /**
     * maximum number of items in a tower.
     */
    public static final int MAX_HEIGHT = 6;
    /**
     * number of towers
     */
    public static final int TOWER_NUM = 3;


    private int blockNum;

    /**
     * list of towers for user to move
     */
    private ArrayList<ArrayList<Integer>> towerList= new ArrayList<>(); // 三个array list的数组

    /**
     * list of target towers;
     */
    private  ArrayList<ArrayList<Integer>> targetList= new ArrayList<>(); // 三个array list的数组



    /**
     *  number of items in the game.
     */
    private int numBlocks;
    /**
     * create random target towers
     * @param blockNum
     */
    public Game(int blockNum) {
        this.blockNum = blockNum;
        if (blockNum > MAX_NUM || blockNum < 1) {
            //System.out.println("Invalid level");
            //return;
            throw new IllegalArgumentException("index out of range: " + blockNum);
        }
        setTowers(towerList, blockNum);
        do {
            targetList = new ArrayList<>();
            setTowers(targetList, blockNum);
        } while (compareTwo(targetList, towerList) == true);

    }

    public boolean isValidMove(int from, int to) {
        if (from < 0 || from >= MAX_NUM || to < 0 || to > MAX_NUM) {
            return false;
        }
        if (towerList.get(from).size() == 0) {
            return false;
        }
        if (towerList.get(to).size() == MAX_HEIGHT) {
            return false;
        }
        return true;
    }

    /**
     * @param from one arrayList
     * @param to the target arrayList
     */
    public void move(int from, int to) {
        if (isValidMove(from, to)) {
            ArrayList<Integer> fromTower = towerList.get(from);
            ArrayList<Integer> toTower = towerList.get(to);
            Integer removed = fromTower.remove(fromTower.size() - 1);
            toTower.add(removed);
        } else {
            throw new IllegalArgumentException("bad move: " + from + " -> " + to);
        }
    }

    public int getBlockNum() {
        return blockNum;
    }

    /**
     * @param index position of the tower.
     */
    public ArrayList<Integer> getBlocksAt(int index) {
        if (index < 0 || index >= MAX_NUM) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        return new ArrayList<>(towerList.get(index));
    }

    /**
     * @param index position of the tower.
     */
    public ArrayList<Integer> getTargetsAt(int index) {
        if (index < 0 || index >= MAX_NUM) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        return new ArrayList<>(targetList.get(index));
    }

    /**
     * compare two towerList
     * @return
     */
    private static boolean compareTwo(ArrayList<ArrayList<Integer>> first, ArrayList<ArrayList<Integer>> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int i = 0; i  < first.size(); i++) {
            if (first.get(i).size() != second.get(i).size() ) {
                return false;
            }
            for (int j = 0; j < first.get(i).size(); j++ ) {
                if (!first.get(i).get(j).equals(second.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * print
     */
    public void print() {
        System.out.println("Current ");
        for (int i = 0; i < TOWER_NUM; i++) {
            System.out.println(towerList.get(i));
        }
        System.out.println("Target ");
        for (int i = 0; i < TOWER_NUM; i++) {
            System.out.println(targetList.get(i));
        }
    }

    /**
     *
     * @param toSet towerList to be set
     */
    private void setTowers(ArrayList<ArrayList<Integer>> toSet, int numBlocks) {
        for (int i = 0; i < TOWER_NUM; i++) {
            toSet.add(new ArrayList<Integer>());
        }
        for (int j = 0; j < numBlocks; j++) {
            int towerPos;
            do {
                towerPos = Math.abs(rand.nextInt()) % TOWER_NUM;
            } while (toSet.get(towerPos).size() >= MAX_HEIGHT);
            toSet.get(towerPos).add(j);
        }
    }

    public int getTopBlockAt(int index) {
        if (index < 0 || index > TOWER_NUM) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        if (towerList.get(index).size() == 0) {
            throw new IllegalArgumentException("empty tower " + index);
        }
        return towerList.get(index).get(towerList.get(index).size() - 1);
    }

    /**
     * whether the game has ended or not.
     */
    public boolean hasEnded() {
        //return Objects.deepEquals(towerList, targetList);
        return compareTwo(towerList, targetList);
    }

    public boolean isTowerEmpty(int index) {
        if (towerList.get(index).size() == 0) {
            return true;
        }
        return false;
    }

    public boolean isTowerFull(int index) {
        if (towerList.get(index).size() == MAX_HEIGHT) {
            return true;
        }
        return false;
    }


}

