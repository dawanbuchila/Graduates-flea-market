<template>
<!-- 新增代码： 首页数据 + echart表格 -->
  <div class="app-container home">
    <el-row :gutter="32">
      <el-col :xs="12" :sm="6" style="text-align: center;margin-bottom: 32px;">
        <el-card shadow="hover" @click.native="selectCard (0)">
          <p style="font-size: 15px; font-weight: bold;">总人数</p>

          <h2>{{userNum}}</h2>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" style="text-align: center;margin-bottom: 32px;">
        <el-card shadow="hover" @click.native="selectCard (1)">
                    <p style="font-size: 15px; font-weight: bold;">总分类数</p>

          <h2>{{categoryNum}}</h2>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" style="text-align: center;margin-bottom: 32px;">
        <el-card shadow="hover" @click.native="selectCard (2)">
                    <p style="font-size: 15px; font-weight: bold;">今日新增帖子数</p>

          <h2>{{addContentNum}}</h2>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" style="text-align: center;margin-bottom: 32px;">
        <el-card shadow="hover" @click.native="selectCard (3)">
                    <p style="font-size: 15px; font-weight: bold;">今日在线人数</p>

          <h2>{{onlineNum}}</h2>
        </el-card>
      </el-col>
    </el-row>


    <el-row type="flex" justify="center">
      <el-col :span="24" style="text-align: center;">
        <div class="bar-chart">
    <div ref="barChart" class="chart"></div>
  </div>
      </el-col>
    </el-row>

        <el-row type="flex" justify="center">
<el-col :span="24" style="text-align: center;">
        <div class="line-chart">
<div class="input-group">
  <label for="startDate">开始时间:</label>
  <el-date-picker v-model="startDate" type="date" placeholder="选择日期" :picker-options="startOptions"></el-date-picker>
  <label for="endDate">结束时间:</label>
  <el-date-picker v-model="endDate" type="date" placeholder="选择日期" :picker-options="endOptions"></el-date-picker>
  <el-button type="primary" @click="getAddUserData">提交</el-button>
</div>
    <div ref="lineChart" class="chart"></div>
  </div>
      </el-col>

    </el-row>

  </div>
</template>

<script>
import * as echarts from 'echarts';
import { getStaticData ,getDateUser } from "@/api/campus/index";
import moment from 'moment';


export default {
  name: "Index",
  data() {
    return {
      userNum:'',
      categoryNum:'',
      addContentNum:'',
      onlineNum:'',
       startDate: '',
      endDate: '',
      // 模拟数据
      userData: {
        dates: ['2022-01-01', '2022-01-02', '2022-01-03', '2022-01-04', '2022-01-05', '2022-01-06', '2022-01-07'],
        data: [150, 232, 201, 154, 190, 330, 410]
      },
      // 版本号
      version: "3.8.4",
      // 模拟数据
      postData: {
        categories: [],
        data: []
      },
      startOptions: {
        format: 'yyyy-MM-dd'
      },
      endOptions: {
        format: 'yyyy-MM-dd'
      }
    };
  },
  mounted() {

      // 获取初始化数据
      this.initData()

      // 新增用户添加默认时间
      this.setDefaultDates();

      // 获取用户添加数据
      this.getAddUserData();

      // 初始化表2
      this.renderLineChart();

      // 初始化表1
      this.renderBarChart()

    },
  methods: {

    initData() {
      getStaticData().then(res => {
        this.userNum = res.data.userNum;
        this.categoryNum = res.data.categoryNum;
        this.addContentNum = res.data.addContentNum;
        this.onlineNum = res.data.onlineNum;

        this.postData.categories = res.data.categoryNameList;
        this.postData.data = res.data.categoryNumList;
// 初始化表1
      this.renderBarChart()
      });
    },

    getAddUserData() {
      const formattedStartDate = moment(this.startDate).format('YYYY-MM-DD');
      const formattedEndDate = moment(this.endDate).format('YYYY-MM-DD');
      console.log("--------------")
      console.log(this.startDate,this.endDate)
      console.log("--------------")

      getDateUser(formattedStartDate,formattedEndDate).then(res => {
        this.userData.dates = res.data.addDate;
        this.userData.data = res.data.addNum;
        // 初始化表2
      this.renderLineChart();
      })
    },

    goTarget(href) {
      window.open(href, "_blank");
    },
    renderBarChart() {
      const chart = echarts.init(this.$refs.barChart);
      const option = {
        title: {
          text: '帖子分类数量表'
        },
        tooltip: {},
        xAxis: {
          data: this.postData.categories
        },
        yAxis: {},
        series: [{
          name: '帖子数量',
          type: 'bar',
          data: this.postData.data,
           barWidth: '25%'
        }]
      };
      chart.setOption(option);
    },
    

 setDefaultDates() {
      const today = new Date().toISOString().substr(0, 10);
      this.endDate = today;
      const lastWeek = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().substr(0, 10);
      this.startDate = lastWeek;
    },
    renderLineChart() {
      const chart = echarts.init(this.$refs.lineChart);
      const option = {
        title: {
          text: '新增用户数量'
        },
        tooltip: {},
        xAxis: {
          type: 'category',
          data: this.userData.dates
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '新增用户数量',
          type: 'line',
          data: this.userData.data
        }]
      };
      chart.setOption(option);
    }









  },
};
</script>

<style scoped lang="scss">



.input-group {
  margin-bottom: 20px;
}

.input-group label {
  margin-right: 10px;
}


.chart {
  height: 400px;
}
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }
  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>

