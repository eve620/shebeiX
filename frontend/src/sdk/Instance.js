//单例模式
import axios from 'axios';

// export const baseURL = 'http://localhost:9000'
// export const baseURL = '/api'


const $axios = axios.create({
    // baseURL:'/',
    baseURL: 'http://localhost:9000',
    withCredentials: true
});

let instance;

export default function getInstance() {
    if (!instance) {
        instance = new Instance()
    }
    return instance;
}

class Instance {
    //Common API
    testFileDownload(path) {
        return $axios({
            url: path,
            method: 'head',
        })
    }

    exportExcel(path, data) {
        return $axios({
            url: path + `/download`,
            method: 'post',
            responseType: 'blob',
            data
        })
    }

    importExcel(file, year) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('year', year.toString());

        return $axios({
            url: "item/upload",
            method: 'post',
            data: formData,
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    }

    //Auth API
    whoami() {
        return $axios({
            'url': '/user/whoami',
            'method': 'get',
        })
    }

    loginApi(data) {
        return $axios({
            'url': '/user/login',
            'method': 'post',
            data
        })
    }

    logoutApi() {
        return $axios({
            'url': '/user/logout',
            'method': 'post',
        })

    }

    //Item API
    getItemList(year, labName) {
        return $axios({
            url: '/item/page',
            method: 'get',
            params: {
                year,
                labName
            }
        })
    }

    deleteItemById(id) {
        return $axios({
            url: `/item/${id}`,
            method: 'delete'
        })
    }

    addItem(params) {
        return $axios({
            url: '/item',
            method: 'post',
            data: {...params}
        })
    }

    editItem(params) {
        return $axios({
            url: '/item',
            method: 'put',
            data: {...params}
        })
    }

    //Lab API
    getLabList(input) {
        return $axios({
            url: '/lab/page',
            method: 'get',
            params: {
                input
            }
        })
    }

    deleteLabById(id) {
        return $axios({
            url: `/lab/${id}`,
            method: 'delete'
        })
    }

    addLab(params) {
        return $axios({
            url: '/lab',
            method: 'post',
            data: {...params}
        })
    }

    editLab(params) {
        return $axios({
            url: '/lab',
            method: 'put',
            data: {...params}
        })
    }

    getAllLabList() {
        return $axios({
            url: '/lab/list',
            method: 'get',
        })
    }

    //User API
    getUserList(input) {
        return $axios({
            url: '/user/page',
            method: 'get',
            params: {
                input,
                getAdmin: false
            }
        })
    }

    deleteUserById(id) {
        return $axios({
            url: `/user/${id}`,
            method: 'delete'
        })
    }

    changeUserPassword(id, params) {
        return $axios({
            url: '/user',
            method: 'put',
            data: {
                userId: id,
                userPassword: params
            }
        })
    }

    addUser(params) {
        return $axios({
            url: '/user',
            method: 'post',
            data: {
                ...params,
                roleId: 0
            }
        })
    }

    editUser(params) {
        return $axios({
            url: '/user',
            method: 'put',
            data: {
                ...params,
                userPassword: null
            }
        })
    }

    getAllUserList() {
        return $axios({
            url: '/user/list',
            method: 'get'
        })
    }

    //Manager API
    getManagerList(input) {
        return $axios({
            url: '/user/page',
            method: 'get',
            params: {
                input,
                getAdmin: true
            }
        })
    }

    addManager(params) {
        return $axios({
            url: '/user',
            method: 'post',
            data: {
                ...params,
                roleId: 1
            }
        })
    }

    //Year API
    deleteYearByName(name) {
        return $axios({
            url: `/year/${name}`,
            method: 'delete'
        })
    }

    addYear(params) {
        return $axios({
            url: '/year',
            method: 'post',
            data: {...params}
        })
    }

    editYear(params) {
        return $axios({
            url: '/year',
            method: 'put',
            data: {...params}
        })
    }

    getYearList() {
        return $axios({
            url: '/year/list',
            method: 'get',
        })
    }

    //File API
    getFileList(parent) {
        return $axios({
            url: '/fileStorage/list',
            method: 'get',
            params: {
                parent
            }
        })
    }

    checkFile(md5, filePath) {
        return $axios({
            url: `/fileStorage/check`,
            method: 'get',
            params: {md5, filePath}
        })
    }

    uploadFileChunk(params) {
        return $axios({
            url: `/fileStorage/upload`,
            method: 'post',
            data: params
        })
    }

    createDir(dirPath) {
        return $axios({
            url: `/fileStorage/createDir`,
            method: 'post',
            headers: {
                'Content-Type': 'text/plain' // 指定发送的是纯文本
            },
            data: dirPath
        })
    }

    deleteFileOrDir(deleteId) {
        return $axios({
            url: "/fileStorage/delete",
            method: "delete",
            params: {deleteId}
        })
    }

    deleteChunks(md5) {
        return $axios({
            url: "/fileStorage/deleteChunks",
            method: "delete",
            params: {md5}
        })
    }

    renameFileOrDir(renameId, newName) {
        return $axios({
            url: "/fileStorage/rename",
            method: "put",
            params: {renameId, newName}
        })
    }
}