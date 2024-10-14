import getInstance from "@/sdk/Instance.js";
import SparkMD5 from 'spark-md5'

const CHUNK_SIZE = 1024 * 1024;
const instance = getInstance()

export class UploadFile {
    constructor(file, maxUploadNum) {
        this.file = file;
        this.isPaused = false; // 暂停状态
        this.currentChunk = 0; // 当前已上传的chunk索引
        this.uploadedChunks = []
        this.totalChunks = Math.ceil(this.file.size / CHUNK_SIZE) === 0 ? 1 : Math.ceil(this.file.size / CHUNK_SIZE);
        this.maxUploadNum = maxUploadNum??3
        this.isCompleted = false; // 新增标志变量
    }

    computeMD5() {
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
    async request(relativePath,dirPath){
        if (this.uploadedChunks.includes(this.currentChunk + 1)) {
            this.currentChunk++
            if (this.currentChunk < this.totalChunks) {
                this.request(relativePath,dirPath)
            }
        } else if(!this.isPaused){
            const start = this.currentChunk * CHUNK_SIZE;
            const end = Math.min(this.file.size, start + CHUNK_SIZE);
            const chunk = this.file.slice(start, end);
            const fileChunk = new File([chunk], this.file.name, {type: this.file.type});
            const formData = new FormData();
            formData.append('chunkNumber', this.currentChunk + 1);
            formData.append('totalChunks', this.totalChunks);
            formData.append('chunkSize', CHUNK_SIZE); // 使用实际的slice大小
            formData.append('currentChunkSize', end - start); // 使用实际的slice大小
            formData.append('totalSize', this.file.size); // 使用整个文件的大小
            formData.append('identifier', this.md5Hash);
            formData.append('filename', this.file.name); // 使用文件的实际名称
            formData.append('relativePath', relativePath); // 如果你需要相对路径W
            formData.append('dirPath', dirPath || ""); // 如果你需要绝对路径
            formData.append('file', fileChunk); // 添加文件切片
            try {
                await instance.uploadFileChunk(formData)
                // 后续若需要并发，promise.race
                // instance.uploadFileChunk(formData).then(res => {
                //     console.log(res)
                // }).catch(e => {
                //     console.log(e)
                // })
                this.currentChunk++;
                if (this.currentChunk < this.totalChunks) {
                    await this.request(relativePath,dirPath);
                } else {
                    this.isCompleted = true; // 标记上传完成
                    console.log(`${this.file.name}上传完成`);
                }
            } catch (error) {
                console.error(`Error uploading chunk ${this.currentChunk} of file ${this.file.name}:`, error);
            }
        }
    }
    async upload(relativePath, dirPath) {
        this.currentChunk = 0
        this.md5Hash = await this.computeMD5();
        await instance.checkFile(this.md5Hash, dirPath ? dirPath + "/" + relativePath : relativePath).then((res) => {
            if (res.data.code === 1) {
                if (res.data.data.uploaded) {
                    this.currentChunk = this.totalChunks
                    this.isCompleted = true; // 标记上传完成
                    console.log(`${this.file.name}已存在，无需上传`);
                }
                this.uploadedChunks = res.data.data.uploadedChunks
            }
        })
        if (!this.isCompleted) {
            for (let i = 0; i < Math.min(this.maxUploadNum,this.totalChunks); i++) {
                this.request(relativePath,dirPath);
            }
        }
        // while (this.currentChunk < this.totalChunks) {
        //     if (this.uploadedChunks.includes(this.currentChunk + 1)) {
        //         this.currentChunk++
        //         continue
        //     }
        //     if (this.isPaused) {
        //         break;
        //     }
        //     const start = this.currentChunk * CHUNK_SIZE;
        //     const end = Math.min(this.file.size, start + CHUNK_SIZE);
        //     const chunk = this.file.slice(start, end);
        //     const fileChunk = new File([chunk], this.file.name, {type: this.file.type});
        //     const formData = new FormData();
        //     formData.append('chunkNumber', this.currentChunk + 1);
        //     formData.append('totalChunks', this.totalChunks);
        //     formData.append('chunkSize', CHUNK_SIZE); // 使用实际的slice大小
        //     formData.append('currentChunkSize', end - start); // 使用实际的slice大小
        //     formData.append('totalSize', this.file.size); // 使用整个文件的大小
        //     formData.append('identifier', this.md5Hash);
        //     formData.append('filename', this.file.name); // 使用文件的实际名称
        //     formData.append('relativePath', relativePath); // 如果你需要相对路径W
        //     formData.append('dirPath', dirPath || ""); // 如果你需要绝对路径
        //     formData.append('file', fileChunk); // 添加文件切片
        //     try {
        //         await instance.uploadFileChunk(formData)
        //         // 后续若需要并发，promise.race
        //         // instance.uploadFileChunk(formData).then(res => {
        //         //     console.log(res)
        //         // }).catch(e => {
        //         //     console.log(e)
        //         // })
        //         this.currentChunk++;
        //     } catch (error) {
        //         console.error(`Error uploading chunk ${this.currentChunk} of file ${this.file.name}:`, error);
        //         break;
        //     }
        // }
        //
        // if (!this.isPaused && this.currentChunk === this.totalChunks) {
        //     console.log(`${this.file.name}上传完成`);
        // }
    }

    pause() {
        this.isPaused = true;
    }

    resume() {
        this.isPaused = false;
// 如果当前chunk为0，则直接调用upload开始上传，否则不需要重新调用upload，因为循环会自动继续
        this.upload();
    }
}