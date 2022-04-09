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
    private int currentpage;
    private int pagesize;
    private int startindex;
    private String filepath;
    private String searchcontent;

    private List list;         //一页的所有记录
    private int totalrecord;   //总共多少条记录
    private int totalpage;     //总共多少页
    private int previouspage;  //想看的页的前一页
    private int nextpage;      //想看的页的下一页
    private int[] pagebar;     //底下的 1 2 3 ...页码条

    public int getStartindex() {
        this.startindex = (this.currentpage - 1) * this.pagesize;
        return startindex;
    }

    public int getTotalpage() {
        if(this.totalrecord == 0){
            return 1;
        }else if(this.totalrecord % this.pagesize == 0){
            this.totalpage = this.totalrecord / this.pagesize;
        }else{
            this.totalpage = this.totalrecord / this.pagesize + 1;
        }
        return totalpage;
    }

    public int getPreviouspage() {
        if(this.currentpage - 1 < 1){
            this.previouspage = 1;
        }else{
            this.previouspage = this.currentpage - 1;
        }
        return previouspage;
    }

    public int getNextpage() {
        if(this.currentpage + 1 >= this.totalpage){
            this.nextpage = this.totalpage;
        }else{
            this.nextpage = this.currentpage + 1;
        }
        return nextpage;
    }

    public int[] getPagebar() {
        int startpage;
        int endpage;
        int pagebar[] = null;
        if(this.totalpage <= 10){                //如果页码总共不超过10页，全部显示出来就好了
            pagebar = new int[this.totalpage];
            startpage = 1;
            endpage = this.totalpage;
        }else{                                //总页数大于10，显示邻近的10页
            pagebar = new int[10];
            startpage = this.currentpage - 4;
            endpage = this.currentpage + 5;
            if(startpage < 1){               //确保不能越界
                startpage = 1;
                endpage = 10;
            }
            if(endpage > this.totalpage){
                endpage = this.totalpage;
                startpage = this.totalpage - 9;
            }
        }
        int index = 0;
        for(int i = startpage; i <= endpage; i++){
            pagebar[index++] = i;
        }
        this.pagebar = pagebar;
        return this.pagebar;
    }

}
