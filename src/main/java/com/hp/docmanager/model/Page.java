package com.hp.docmanager.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 16:32
 */

@Component("page")
@Scope("prototype")
@Data
@NoArgsConstructor
public class Page {
    private int currentPage;
    private int pageSize;
    private int startIndex;
    private String filepath;
    private String searchContent;

    private List list;         //一页的所有记录
    private int totalRecord;   //总共多少条记录
    private int totalPage;     //总共多少页
    private int previousPage;  //想看的页的前一页
    private int nextPage;      //想看的页的下一页
    private int[] pageBar;     //底下的 1 2 3 ...页码条

    public int getStartIndex() {
        this.startIndex = (this.currentPage - 1) * this.pageSize;
        return startIndex;
    }

    public int getTotalPage() {
        if(this.totalRecord == 0){
            return 1;
        }else if(this.totalRecord % this.pageSize == 0){
            this.totalPage = this.totalRecord / this.pageSize;
        }else{
            this.totalPage = this.totalRecord / this.pageSize + 1;
        }
        return totalPage;
    }

    public int getPreviousPage() {
        if(this.currentPage - 1 < 1){
            this.previousPage = 1;
        }else{
            this.previousPage = this.currentPage - 1;
        }
        return previousPage;
    }

    public int getNextPage() {
        if(this.currentPage + 1 >= this.totalPage){
            this.nextPage = this.totalPage;
        }else{
            this.nextPage = this.currentPage + 1;
        }
        return nextPage;
    }

    public int[] getPageBar() {
        int startpage;
        int endpage;
        int pagebar[] = null;
        if(this.totalPage <= 10){                //如果页码总共不超过10页，全部显示出来就好了
            pagebar = new int[this.totalPage];
            startpage = 1;
            endpage = this.totalPage;
        }else{                                //总页数大于10，显示邻近的10页
            pagebar = new int[10];
            startpage = this.currentPage - 4;
            endpage = this.currentPage + 5;
            if(startpage < 1){               //确保不能越界
                startpage = 1;
                endpage = 10;
            }
            if(endpage > this.totalPage){
                endpage = this.totalPage;
                startpage = this.totalPage - 9;
            }
        }
        int index = 0;
        for(int i = startpage; i <= endpage; i++){
            pagebar[index++] = i;
        }
        this.pageBar = pagebar;
        return this.pageBar;
    }

}
