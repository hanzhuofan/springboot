package com.hzf.study.program.nowcoder;

import java.util.Stack;

/**
 * @author zhuofan.han
 * @date 2022/1/28
 */
public class MergeList {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();

        MergeList test = new MergeList();
        ListNode list1 = new ListNode(1);
        ListNode list3 = new ListNode(3);
        ListNode list5 = new ListNode(5);
        list1.next = list3;
        list3.next = list5;
        ListNode list2 = new ListNode(2);
        ListNode list4 = new ListNode(4);
        ListNode list6 = new ListNode(6);
        list2.next = list4;
        list4.next = list6;
        ListNode merge = test.Merge(list1, list2);
        System.out.println(merge);
    }
    public ListNode Merge(ListNode list1,ListNode list2) {
        if (list1 == null && list2 == null) {
            return null;
        }
        ListNode mergeList = new ListNode(-1000);
        merge(list1, list2, mergeList);
        return mergeList.next;
    }

    private void merge(ListNode list1, ListNode list2, ListNode mergeList) {
        if (list1 == null) {
            mergeList.next = list2;
            return;
        }
        if (list2 == null) {
            mergeList.next = list1;
            return;
        }

        if (list1.val < list2.val) {
            mergeList.next = list1;
            merge(list1.next, list2, mergeList.next);
        } else {
            mergeList.next = list2;
            merge(list1, list2.next, mergeList.next);
        }
    }

    public void merge(int A[], int m, int B[], int n) {
        for (int i = m + n - 1; i >= 0; i--) {
            if (n == 0) {
                A[i] = A[m-- - 1];
            } else if (m == 0) {
                A[i] = B[n-- - 1];
            } else if (A[m - 1] > B[n - 1]) {
                A[i] = A[m-- - 1];
            } else {
                A[i] = B[n-- - 1];
            }
        }
    }

    static class ListNode {
        int val;
        ListNode next = null;

        public ListNode(int val) {
            this.val = val;
        }
    }
}
