import request from '@/utils/request'

export default {
    //获取用户信息
    getUserInfo() {
        return request({
            url: `/getInfo`,
            method: `get`
        })
    },
    //判断有没有绑定邮箱
    haveMail() {
        return request({
            url: "/campus/haveMail",
            method: `get`
        })
    },


    // 查询用户个人信息
    getUserProfile() {
        return request({
            url: '/system/user/profile',
            method: 'get'
        })
    },

    // 用户密码重置
    updateUserPwd(oldPassword, newPassword) {
        const data = {
            oldPassword,
            newPassword
        }
        return request({
            url: '/system/user/profile/updatePwd',
            method: 'put',
            params: data
        })
    },

    // 用户头像上传
    uploadAvatar(data) {
        return request({
            url: '/system/user/profile/avatar',
            method: 'post',
            data: data
        })
    },


    // 修改用户个人信息
    updateUserProfile(data) {
        return request({
            url: '/system/user/profile/update',
            method: 'put',
            data: data
        })
    },

    //给邮箱发送修改密码的验证码
    sendResetPwdCode(data) {
        return request({
            url: '/campus/pwd-code',
            headers: {
                isToken: false
            },
            method: 'post',
            data: data
        })
    },
    //修改密码
    changePwd(data) {
        return request({
            url: '/campus/changePwd',
            headers: {
                isToken: false
            },
            method: 'post',
            data: data
        })
    },
    changePwdByEmail(data) {
        return request({
            url: '/campus/changePwdByEmail',
            headers: {
                isToken: false
            },
            method: 'post',
            data: data
        })
    }

}