import getInstance from "@/sdk/Instance.js";
import SparkMD5 from 'spark-md5'

const CHUNK_SIZE = 1024 * 1024;
const instance = getInstance()
const check = Symbol("check");
const request = Symbol("request");
const computeMD5 = Symbol("computeMD5");

export class UploadFile {
    constructor(file, dirPath, relativePath, maxUploadNum) {
        this.file = file;
        this.isPaused = false; // 暂停状态
        this.currentChunk = 0; // 当前已上传的chunk索引
        this.uploadedChunks = []
        this.totalChunks = Math.ceil(this.file.size / CHUNK_SIZE) === 0 ? 1 : Math.ceil(this.file.size / CHUNK_SIZE);
        this.maxUploadNum = maxUploadNum ?? 3
        this.isCompleted = false; // 新增标志变量
        this.relativePath = relativePath
        this.dirPath = dirPath ?? ""
    }

    async upload() {
        try {
            this.md5Hash = await this[computeMD5]();
            await this[check]()
            if (this.isCompleted) {
                return {status: 1, message: `上传成功`};
            }
            this.currentChunk = 0
            const promises = [];
            for (let i = 0; i < Math.min(this.maxUploadNum, this.totalChunks); i++, this.currentChunk++) {
                promises.push(this[request]());
            }
            await Promise.all(promises);
            if (this.isCompleted) {
                return {status: 1, message: `上传成功`};
            } else {
                return {status: 2, message: `已暂停`};
            }
        } catch (error) {
            this.isPaused = true
            return {status: 0, message: `上传文件失败: ${error.message}`};
        }
    }

    pause() {
        this.isPaused = true;
    }

    async resume() {
        this.isPaused = false;
        return await this.upload();
    }

    async [request]() {
        // 使用 Set 存储已上传的块
        const uploadedChunksSet = new Set(this.uploadedChunks);
        // 找到下一个未上传的块
        while (this.currentChunk < this.totalChunks && uploadedChunksSet.has(this.currentChunk + 1)) {
            this.currentChunk++;
        }

        if (this.currentChunk < this.totalChunks && !this.isPaused) {
            const start = this.currentChunk * CHUNK_SIZE;
            const end = Math.min(this.file.size, start + CHUNK_SIZE);
            const chunk = this.file.slice(start, end);
            const fileChunk = new File([chunk], this.file.name, {type: this.file.type});
            const formData = new FormData();
            formData.append('chunkNumber', this.currentChunk + 1);
            formData.append('totalChunks', this.totalChunks);
            formData.append('chunkSize', CHUNK_SIZE);
            formData.append('currentChunkSize', end - start);
            formData.append('totalSize', this.file.size);
            formData.append('identifier', this.md5Hash);
            formData.append('fileName', this.file.name);
            formData.append('relativePath', this.relativePath);
            formData.append('dirPath', this.dirPath || "");
            formData.append('file', fileChunk);

            try {
                await instance.uploadFileChunk(formData);
                this.currentChunk++;
                if (this.currentChunk < this.totalChunks) {
                    await this[request]();
                } else {
                    this.isCompleted = true;
                }
            } catch (error) {
                // 分析错误类型
                if (error.message) {
                    if (error.message === "Network Error") throw new Error("网络错误");
                    else throw new Error(error.message);
                } else {
                    throw new Error('其他错误');
                }
            }
        }
    }

    [computeMD5]() {
        return new Promise((resolve, reject) => {
            let currentChunk = 0;
            let spark = new SparkMD5.ArrayBuffer();
            const fileReader = new FileReader();

            fileReader.onload = function (e) {
                spark.append(e.target.result); // Append array buffer
                currentChunk++;

                if (currentChunk < this.totalChunks) {
                    loadNext();
                } else {
                    // Convert the final hash to a hexadecimal string
                    resolve(spark.end());
                }
            };

            fileReader.onerror = reject;
            const self = this;

            function loadNext() {
                const start = currentChunk * CHUNK_SIZE;
                const end = ((start + CHUNK_SIZE) >= self.file.size) ? self.file.size : start + CHUNK_SIZE;
                fileReader.readAsArrayBuffer(self.file.slice(start, end));
            }

            loadNext(); // Start loading the file
        });
    }

    async [check]() {
        try {
            const res = await instance.checkFile(this.md5Hash, this.dirPath ? this.dirPath + "/" + this.relativePath : this.relativePath);
            if (res.data.code === 1) {
                if (res.data.data.uploaded) {
                    this.currentChunk = this.totalChunks;
                    this.isCompleted = true; // 标记上传完成
                }
                this.uploadedChunks = res.data.data.uploadedChunks;
            }
        } catch (error) {
            throw new Error('网络错误');
        }
    }
}

