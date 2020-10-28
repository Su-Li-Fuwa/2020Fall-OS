import java.util.LinkedList;

public class MyTry {
    public static void main(String[] args) {
        LinkedList<Integer> waitList = new LinkedList<Integer>();
        LinkedList<LinkedList<Integer>> set = new LinkedList<LinkedList<Integer>>();
        waitList.add(1);
        set.add(waitList);
        System.out.println(set);
        waitList.add(2);
        System.out.println(waitList);
        System.out.println(set);
        set.remove(waitList);
        System.out.println(set);
    }
}
