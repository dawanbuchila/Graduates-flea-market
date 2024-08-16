<template>
  <div class="resetPwd">
    <div class="reset-step">
      <h3 class="title">重置密码</h3>
      

        <div>
          <el-form
            ref="resetForm1"
            :model="resetForm1"
            :rules="resetForm1Rules"
            class="reset-form"
          >
            <el-form-item prop="email">
              <el-input
                v-model="resetForm1.email"
                type="text"
                auto-complete="off"
                placeholder="请输入邮箱"
              >
                <svg-icon
                  slot="prefix"
                  icon-class="user"
                  class="el-input__icon input-icon"
                />
              </el-input>
            </el-form-item>
            

            <el-form-item prop="code">
              <el-input
                v-model="resetForm1.password"
                auto-complete="off"
                placeholder="新密码"
                style="width: 63%"
              >
                <svg-icon
                  slot="prefix"
                  icon-class="validCode"
                  class="el-input__icon input-icon"
                />
              </el-input>
              
            </el-form-item>

            <el-form-item style="width: 100%">
              <el-button
                style="width: 100%"
                :loading="loading"
                type="success"
                @click="handleUpdatePassword"
                >确认</el-button
              >
            </el-form-item>
          </el-form>
        </div>


    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import userInfoApi from "@/api/userInfo";
import { getToken, setToken, removeToken } from "@/utils/auth";

export default {
  name: "resetPwd",
  data() {
    return {
      active: 0,
      resetForm1: {
        email: "",
        password: "",
      },
      resetForm1Rules: {
        
        // code: [{ required: true, trigger: "change", message: "请出入密码" }],
        // email: [{ required: true, trigger: "change", message: "请输入邮箱" }],
      },
      resetForm2Rules: {
        
      },

      codeUrl: "",
      captchaEnabled: true,
      loading: false,
    };
  },
  created() {
    this.getCode();
  },
  methods: {
    handleUpdatePassword() {
      this.$refs.resetForm1.validate((valid) => {
        if (valid) {
          userInfoApi.changePwdByEmail(this.resetForm1)
            .then((respose) => {
              this.$message({
                type: "success",
                message: "修改成功",
              });
              // removeToken();
              // this.resetForm1.code = "";
              // this.active = 0;
              var count = 3; //赋值多少秒
              var times = setInterval(() => {
                count--; //递减
                if (count <= 0) {
                  clearInterval(times);
                  this.$router.push({ path: "/userlogin" });
                } else {
                  this.$message.warning(
                    "将再 " + count + " 秒后跳转到登录页面"
                  );
                }
              }, 1000); //1000毫秒后执行
            })
            .catch((response) => {
              this.getCode();
            });
        }
      });
    },
    getCode() {
      getCodeImg().then((res) => {
        this.captchaEnabled =
          res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.resetForm1.uuid = res.uuid;
        }
      });
    },
    handleEmail() {
      this.$refs.resetForm1.validate((valid) => {
        if (valid) {
          userInfoApi
            .sendResetPwdCode(this.resetForm1)
            .then((respose) => {
              this.$message({
                type: "success",
                message: "正在发送验证码到邮箱!",
              });
              this.resetForm1.code = "";
              this.active = 1;
            })
            .catch((response) => {
              this.getCode();
            });
        }
      });
    },
    handlePwd() {
      this.$refs.resetForm2.validate((valid) => {
        if (valid) {
          userInfoApi
            .changePwd(this.resetForm1)
            .then((respose) => {
              this.$message({
                type: "success",
                message: "修改成功!",
              });
              removeToken();
              // this.resetForm1.code = "";
              // this.active = 0;
              var count = 3; //赋值多少秒
              var times = setInterval(() => {
                count--; //递减
                if (count <= 0) {
                  clearInterval(times);
                  this.$router.push({ path: "/userlogin" });
                } else {
                  this.$message.warning(
                    "将再 " + count + " 秒后跳转到登录页面"
                  );
                }
              }, 1000); //1000毫秒后执行
            })
            .catch((response) => {});
        }
      });
    },
  },
};
</script>

<style>
.resetPwd {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.reset-step {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}
.reset-con {
  margin-top: 20px;
  padding: 20px;
}

.el-input input {
  height: 38px;
}
.reset-code {
  width: 33%;
  height: 38px;
  float: right;
}
.reset-code img {
  cursor: pointer;
  vertical-align: middle;
  height: 38px;
}
</style>