package com.hp.securedocdisk.model;

import com.google.common.hash.BloomFilter;

public class TreeNode {
    public BloomFilter<String> value;
    public TreeNode left;
    public TreeNode right;

    // 前序遍历
    public void preOrder() {
        if (this.left != null) {
            this.left.preOrder();
        }
        if (this.right != null) {
            this.right.preOrder();
        }
    }

    public TreeNode(BloomFilter<String> value) {
        this.value = value;
    }

}
