const CHUNK_SIZE = 1024 * 1024;

export class UploadFile {
    constructor(file) {
        this.file = file;
        this.isPaused = false; // 暂停状态
        this.currentChunk = 0; // 当前已上传的chunk索引
    }

    computeMD5() {
        return new Promise((resolve, reject) => {
            const chunks = Math.ceil(this.file.size / CHUNK_SIZE);
            let currentChunk = 0;
            let spark = new SparkMD5.ArrayBuffer();
            const fileReader = new FileReader();

            fileReader.onload = function (e) {
                spark.append(e.target.result); // Append array buffer
                currentChunk++;

                if (currentChunk < chunks) {
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

    async uploadChunk(chunk, chunkNumber, totalChunks, md5Hash) {
// 实现上传单个chunk的逻辑
// 这里应该是实际的上传逻辑，现在假设成功
        return new Promise((resolve) => {
            setTimeout(() => {
                console.log(`Uploading chunk ${chunkNumber} of ${totalChunks} for file ${this.file.name},md5: ${md5Hash}`);
                resolve();
            }, 300);
        });
    }

    async upload() {
        const totalChunks = Math.ceil(this.file.size / CHUNK_SIZE);
        const md5Hash = await this.computeMD5();
        console.log(`Md5 ${md5Hash}`);

        while (this.currentChunk < totalChunks) {
            if (this.isPaused) {
                break;
            }
            const start = this.currentChunk * CHUNK_SIZE;
            const end = Math.min(this.file.size, start + CHUNK_SIZE);
            const chunk = this.file.slice(start, end);
            try {
                await this.uploadChunk(chunk, this.currentChunk + 1, totalChunks, md5Hash);
                this.currentChunk++;
// 进度更新等逻辑...
            } catch (error) {
                console.error(`Error uploading chunk ${this.currentChunk} of file ${this.file.name}:`, error);
                break;
            }
        }

        if (!this.isPaused && this.currentChunk === totalChunks) {
            console.log(`Upload of file ${this.file.name} completed.`);
// 可以在这里处理文件上传完成的后续逻辑
        }
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

function example() {
    // 使用示例
    const file = new File(['file content'], 'example.txt', {type: 'text/plain'});
    const uploadFile = new UploadFile(file);

// 开始上传
    uploadFile.upload().catch(console.error);

// 暂停上传
    setTimeout(() => {
        uploadFile.pause();
        console.log('Upload paused.');
    }, 800); // 例如，2秒后暂停上传，仅作示例

// 模拟一段时间后恢复上传
    setTimeout(() => {
        uploadFile.resume();
        console.log('Upload resumed.');
    }, 2000); // 例如，再过3秒恢复上传，仅作示例

}
