package com.hp.securedocdisk.utils;

import java.util.*;

public class GenTreeUtil {
    public static class BaseTreeNode{
        protected Long id;
        protected Long pid;
        protected Boolean leafNode;
        protected List<BaseTreeNode> childList;

        BaseTreeNode(Long id, Long pid, Boolean leafNode, List<BaseTreeNode> childList) {
            this.id = id;
            this.pid = pid;
            this.leafNode = leafNode;
            this.childList = childList;
        }
    }

    /**
     * 根据叶子节点生成只包含叶子节点分支的树
     * @param allNodeList 整棵树或者整个深林的所有的节点
     * @param leafNodeList 需要展示的叶子节点
     * @return 分支树
     */
    public static List<? extends BaseTreeNode> getBranchTree(List<? extends BaseTreeNode> allNodeList, List<? extends BaseTreeNode> leafNodeList){
        Hashtable<Long, List<BaseTreeNode>> dicNode = new Hashtable<>();
        for (BaseTreeNode baseTreeNode : leafNodeList) {
            List<BaseTreeNode> childList = dicNode.computeIfAbsent(baseTreeNode.pid, k -> new ArrayList<>());
            childList.add(baseTreeNode);
        }

        if(dicNode.isEmpty()){
            return Collections.emptyList();
        }
        while (!(dicNode.containsKey(-1L) && dicNode.size() == 1)){ // 递归出口
            CombineDirectParent(allNodeList, dicNode);
        }
        return dicNode.get(-1L);
    }

    /**
     * 自底向上，每次每个叶子节点上升一层，查找并合并一层的父子关系，通过dicNode里面的父子关系把children并到父节点数据结构的childList中
     * @param allNodeList 所有节点，已经找到祖宗孩子关系的节点就remove，不重复添加
     * @param dicNode 每次只存一层关系合并完就删除，最后只剩下根节点和第一层节点的父子关系
     */
    public static void CombineDirectParent(List<? extends BaseTreeNode> allNodeList, Hashtable<Long, List<BaseTreeNode>> dicNode){
        Iterator<BaseTreeNode> iterator = (Iterator<BaseTreeNode>) allNodeList.iterator();
        while (iterator.hasNext()){
            BaseTreeNode parentNode = iterator.next();
            if(dicNode.containsKey(parentNode.id)){
                List<BaseTreeNode> childList = dicNode.get(parentNode.id);
                // 把父子关系保存在node结构体，dicNode的父子关系就可以remove了
                parentNode.childList.addAll(childList);
                AddOneNode(parentNode, dicNode);
                //  删除已经用完了的下一级，构造dicNode.size==1的递归出口，保证不同树叶子节点都可以递归到根
                dicNode.remove(parentNode.id);
                iterator.remove();
            }
        }
    }
    /**
     * 更新dicNode的父子关系，把当前节点合并到上一层的父子关系或者升级到上一层的父子关系
     * @param node dicNode中的Key对应的节点，也就是value的父节点
     * @param dicNode
     */
    private static void AddOneNode(BaseTreeNode node, Hashtable<Long, List<BaseTreeNode>> dicNode) {
        List<BaseTreeNode> childList = dicNode.get(node.pid);
        if(childList != null){
            childList.add(node);
        }else {
            dicNode.put(node.pid, new ArrayList<>(Arrays.asList(node)));
        }
    }

    /**
     * 传入一棵树或者深林的需要展示的所有节点，生成树
     * @param valueList 需要展示的所有节点
     * @return 一棵树或一个深林
     */
    public static List<?> geFullTree(List<? extends BaseTreeNode> valueList){

        Map<Long, BaseTreeNode> nodeMap = new HashMap<>();
        List<BaseTreeNode> rootNodeList = new ArrayList<>();

        for (BaseTreeNode node : valueList) {
            Long id = node.id;
            Long pid = node.pid;
            BaseTreeNode baseTreeNode = nodeMap.get(id);
            if (baseTreeNode != null) {
                node.childList.addAll(baseTreeNode.childList);
            }
            nodeMap.put(id, node);

            BaseTreeNode parentNode = nodeMap.get(pid);
            if (parentNode != null) {
                if(parentNode.childList == null){
                    parentNode.childList = new ArrayList<>();
                }
                parentNode.childList.add(node);
            }else{
                nodeMap.put(pid, new BaseTreeNode(pid, null, null, new ArrayList<>(Arrays.asList(node))));
            }
            if(new Long(-1L).equals(pid)){
                rootNodeList.add(node);
            }
        }
        return rootNodeList;
    }

    /**
     * 传入所有结点，和需要生成的子树的id
     * @param valueList
     * @param childTreeRootId
     * @return
     */
    public static BaseTreeNode GeChildTree(List<? extends BaseTreeNode> valueList, Long childTreeRootId){
        Map<Long, BaseTreeNode> nodeMap = new HashMap<>();

        for (BaseTreeNode node : valueList) {
            Long id = node.id;
            Long pid = node.pid;
            BaseTreeNode childNode = nodeMap.get(id);
            if (childNode == null) {
                childNode = node;
            }else{
                // 遍历到孩子的时候已经put，只拿childlist复制到node
                if (childNode.childList != null) {
                    if (node.childList == null) {
                        node.childList = new ArrayList<>();
                    }
                    node.childList.addAll(childNode.childList);
                }
                childNode = node;
            }
            // 每次都put为了nodeMap存的是传进来子类类型
            nodeMap.put(id, childNode);

            BaseTreeNode parentNode = nodeMap.get(pid);
            if (parentNode == null) {
                parentNode = new BaseTreeNode(pid, null, false, new ArrayList<>(Arrays.asList(childNode)));
                nodeMap.put(pid, parentNode);
            }else{
                if (parentNode.childList == null) {
                    parentNode.childList = new ArrayList<>();
                }
                parentNode.childList.add(childNode);
            }
        }
        return nodeMap.get(childTreeRootId);
    }
}
