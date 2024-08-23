export class UploadFile {
    constructor(file) {
        this.file = file;
        this.isPaused = false;
        this.currentChunk = 0;
        this.totalChunks = Math.ceil(this.file.size / CHUNK_SIZE) || 1;
        this.uploadPromises = [];
    }

    async computeMD5() {
        const spark = new SparkMD5.ArrayBuffer();
        const fileReader = new FileReader();

        return new Promise((resolve, reject) => {
            const loadNext = (start) => {
                const end = Math.min(this.file.size, start + CHUNK_SIZE);
                fileReader.onload = () => {
                    spark.append(fileReader.result);
                    if (start + CHUNK_SIZE < this.file.size) {
                        loadNext(start + CHUNK_SIZE);
                    } else {
                        resolve(spark.end());
                    }
                };
                fileReader.onerror = reject;
                fileReader.readAsArrayBuffer(this.file.slice(start, end));
            };

            loadNext(0);
        });
    }

    async uploadChunk(chunk, chunkNumber) {
        console.log(`Uploading chunk ${chunkNumber}...`);
        // 这里应该是实际的上传逻辑
        // 假设成功，实际应用中应该替换为真实的上传函数
        await new Promise(resolve => setTimeout(resolve, 300)); // 模拟网络延迟
        console.log(`Chunk ${chunkNumber} uploaded successfully.`);
    }

    async upload() {
        this.md5Hash = await this.computeMD5();
        console.log(`Md5: ${this.md5Hash}`);

        for (let i = 0; i < this.totalChunks; i++) {
            const start = i * CHUNK_SIZE;
            const end = Math.min(this.file.size, start + CHUNK_SIZE);
            const chunk = this.file.slice(start, end);

            // 如果达到最大并发数，则等待一个上传完成
            while (this.uploadPromises.length >= MAX_CONCURRENT_UPLOADS) {
                if (this.isPaused) {
                    break;
                }
                await Promise.race(this.uploadPromises);
            }

            if (this.isPaused) {
                break;
            }

            // 添加新的上传任务到队列
            this.uploadPromises.push(
                this.uploadChunk(chunk, i + 1).then(() => {
                    this.uploadPromises = this.uploadPromises.filter(p => p !== this.uploadPromises[0]);
                    return Promise.resolve();
                })
            );
        }

        // 等待所有上传完成
        await Promise.all(this.uploadPromises);

        if (!this.isPaused) {
            console.log(`${this.file.name} 上传完成`);
        }
    }

    pause() {
        this.isPaused = true;
    }

    resume() {
        this.isPaused = false;
        // 如果当前chunk为0，则直接调用upload开始上传
        if (this.currentChunk === 0) {
            this.upload();
        }
    }
}
