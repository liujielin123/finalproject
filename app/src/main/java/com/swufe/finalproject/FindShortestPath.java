package com.swufe.finalproject;

public class FindShortestPath {

    private int[][] P;
    private int[][] D;
    public final static int INFINITY = Integer.MAX_VALUE;
    public  String[] siteName= new String[]{
            /*1号线：0-21*/
            "升仙湖","火车北站","人民北路","文殊院","骡马市","天府广场","锦江宾馆","华西坝","省体育馆","倪家桥",
            "桐梓林","火车南站","高新","金融城","孵化园","锦城广场","世纪城","天府三街","天府五街","华府大道",
            "四河","广都",
            /*火车北站1(1,7)，骡马市4(1,4)，天府广场5(1,2)，省体育馆8(1,3)，火车南站11(1,7)*/

            /*2号线：22-52*/
            "犀浦","天河路","百草路","金周路","金科北路","迎宾大道","茶店子客运站","羊犀立交","一品天下","蜀汉路东",
            "白果林","中医大省医院","通惠门","人民公园","春熙路","东门大桥","牛王庙","牛市口","东大路","塔子山公园",
            "成都东客站","成渝立交","惠王陵","洪河","成都行政学院","大面铺","连山坡","界牌","书房","龙平路","龙泉驿",
            /*一品天下30(2,7)，中医大省医院33(2,4)，春熙路36(2,3)，成都东客站42(2,7)*/

            /*3号线：53-67*/
            "军区总医院","熊猫大道","动物园","昭觉寺南路","驷马桥","李家沱","前锋路","红星桥","市二医院","新南门",
            "磨子桥","衣冠庙","高升桥","红牌楼","太平园",
            /*驷马桥57(3,7)，市二医院61(3,7)，太平园67(3,7)*/

            /*4号线：68-94*/
            "万盛","杨柳河","凤溪河","南熏大道","光华公园","涌泉","凤凰大街","马厂坝","非遗博览园","蔡桥",
            "中坝","成都西站","清江西路","文化宫","西南财大","草堂北路","宽窄巷子","太升南路","玉双路","双桥路",
            "万年场","槐树店","来龙","十陵","成都大学","明蜀王陵","西河",
            /*文化宫81(4,7)，槐树店89(4,7)*/

            /*7号线：95-117*/
            "八里庄","府青路","北站西二路","九里堤","西南交大","花照壁","茶店子","金沙博物馆","东坡路","龙爪堰",
            "武侯大道","高朋大道","神仙树","三瓦窑","琉璃场","四川师大","狮子山","大观","迎晖路","双店路",
            "崔家店","理工大学","二仙桥",

            /*10号线：118-122*/
            "簇锦","华兴","金花","空港一站","空港二站",
    };
    public int[][] arcs;
    public FindShortestPath() {
        //初始化邻接矩阵
        this.arcs=new int[siteName.length][siteName.length];
        for(int i=0;i<arcs.length;i++) {
            for(int j=0;j<arcs.length;j++) {
                if(i==j)
                    arcs[i][j]=0;
                else
                    arcs[i][j]=INFINITY;
            }
        }
        //添加一号线
        int subway1[]= {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21};
        for(int i=0;i<subway1.length-1;i++) {
            arcs[subway1[i]][subway1[i+1]]=1;
            arcs[subway1[i+1]][subway1[i]]=1;
        }
        //添加二号线
        int subway2[]= {22,23,24,25,26,27,28,29,30,31,32,33,34,35,5,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52};
        for(int i=0;i<subway2.length-1;i++) {
            arcs[subway2[i]][subway2[i+1]]=1;
            arcs[subway2[i+1]][subway2[i]]=1;
        }
        //添加三号线
        int subway3[]= {53,54,55,56,57,58,59,60,61,36,62,63,8,64,65,66,67};
        for(int i=0;i<subway3.length-1;i++) {
            arcs[subway3[i]][subway3[i+1]]=1;
            arcs[subway3[i+1]][subway3[i]]=1;
        }
        //添加四号线
        int subway4[]= {68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,33,84,4,85,61,86,87,88,89,90,91,92,93,94};
        for(int i=0;i<subway4.length-1;i++) {
            arcs[subway4[i]][subway4[i+1]]=1;
            arcs[subway4[i+1]][subway4[i]]=1;
        }
        //添加七号线
        int subway7[]= {95,96,57,1,97,98,99,100,101,30,102,81,103,104,105,67,106,107,11,108,109,110,111,112,42,113,89,114,115,116,117};
        for(int i=0;i<subway7.length-1;i++) {
            arcs[subway7[i]][subway7[i+1]]=1;
            arcs[subway7[i+1]][subway7[i]]=1;
        }
        //添加十号线
        int subway10[]= {67,118,119,120,121,122};
        for(int i=0;i<subway10.length-1;i++) {
            arcs[subway10[i]][subway10[i+1]]=1;
            arcs[subway10[i+1]][subway10[i]]=1;
        }

        this.folyd();
    }

    public void folyd() {
        int vexNum=siteName.length;
        //路径、路径长度矩阵初始化
        P = new int[vexNum][vexNum];
        D = new int[vexNum][vexNum];
        for(int i=0;i<vexNum;i++)
            for(int j=0;j<vexNum;j++) {
                D[i][j] = arcs[i][j];//d初值是图的邻接矩阵
                P[i][j]=i!=j&&arcs[i][j]<INFINITY?i:-1;
            }


        for(int k=0;k<vexNum;k++)
            for(int i=0;i<vexNum;i++)
                for(int j=0;j<vexNum;j++)
                    if(D[i][k] < INFINITY && D[k][j] < INFINITY&&D[i][j]>D[i][k]+D[k][j]) {
                        D[i][j]=D[i][k]+D[k][j];
                        P[i][j]=P[k][j];
                    }
    }

    public String getPath(String start,String end) {//输出p矩阵中从顶点vi到vj的路径字符串
        int i=this.locateVex(start),j=this.locateVex(end);
        String path="";
        if(i!=-1&&j!=-1){
            int n=1;
            String m[]=new String[P.length];
            m[0]=siteName[j];
            for(int k=P[i][j];k!=i&&k!=j&&k!=-1;k=P[i][k]) {
                m[n]=siteName[k];
                n++;
            }
            m[n]=siteName[i];
            n++;
            for(int x=n-1;x>=0;x--) {
                if(x>0)
                    path=path+m[x]+"=>";
                else
                    path=path+m[x];
            }
        }
        else{
            path="正在查询，请等待";
        }
        return path;
    }

    public int locateVex(String vex) {//定位顶点
        for (int v = 0; v < siteName.length; v++)
            if (siteName[v].equals(vex))
                return v;
        return -1;
    }
}

