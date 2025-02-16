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
        this.uploadedChunks = []
        this.pendingChunks = [];
        this.totalChunks = Math.ceil(this.file.size / CHUNK_SIZE) === 0 ? 1 : Math.ceil(this.file.size / CHUNK_SIZE);
        this.maxUploadNum = maxUploadNum ?? 3
        this.isCompleted = false; // 新增标志变量
        this.relativePath = relativePath
        this.dirPath = dirPath ?? ""
    }

    async upload() {
        try {
            this.md5Hash = await this[computeMD5]();
            await this[check]();
            if (this.isCompleted) {
                return { status: 1, message: `上传成功` };
            }

            // 初始化任务队列
            this.pendingChunks = [];
            for (let i = 1; i <= this.totalChunks; i++) {
                if (!this.uploadedChunks.includes(i)) {
                    this.pendingChunks.push(i);
                }
            }

            // 并发上传
            const promises = [];
            for (let i = 0; i < Math.min(this.maxUploadNum, this.pendingChunks.length); i++) {
                promises.push(this[request](this.pendingChunks));
            }
            await Promise.all(promises);

            if (this.isCompleted) {
                return { status: 1, message: `上传成功` };
            } else {
                return { status: 2, message: `已暂停` };
            }
        } catch (error) {
            this.isPaused = true;
            return { status: 0, message: `上传文件失败: ${error.message}` };
        }
    }

    pause() {
        this.isPaused = true;
        this.pendingChunks = []; // 清空队列
    }

    async resume() {
        this.isPaused = false;
        return await this.upload();
    }

    async [request](pendingChunks) {
        while (pendingChunks.length > 0 && !this.isPaused) {
            const chunkNumber = pendingChunks.shift(); // 从队列中取出分片
            if (this.uploadedChunks.includes(chunkNumber)) continue; // 跳过已上传

            const start = (chunkNumber - 1) * CHUNK_SIZE;
            const end = Math.min(this.file.size, start + CHUNK_SIZE);
            const chunk = this.file.slice(start, end);
            const fileChunk = new File([chunk], this.file.name, { type: this.file.type });

            const formData = new FormData();
            formData.append('chunkNumber', chunkNumber);
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
                this.uploadedChunks.push(chunkNumber); // 更新本地状态
                if (this.uploadedChunks.length === this.totalChunks) {
                    this.isCompleted = true;
                }else {
                    await this[request](pendingChunks);
                }
            } catch (error) {
                pendingChunks.push(chunkNumber); // 重新加入队列
                throw error;
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
                    this.isCompleted = true; // 标记上传完成
                }
                this.uploadedChunks = res.data.data.uploadedChunks;
            }
        } catch (error) {
            throw new Error('网络错误');
        }
    }
}

