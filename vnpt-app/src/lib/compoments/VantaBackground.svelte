<script lang="ts">
    import { onMount, onDestroy } from 'svelte';

    let canvas: HTMLCanvasElement;
    let ctx: CanvasRenderingContext2D;
    let animationId: number;
    let stars: Star[] = [];
    let shootingStars: ShootingStar[] = [];

    interface Star {
        x: number;
        y: number;
        size: number;
        brightness: number;
        twinkleSpeed: number;
        twinkleOffset: number;
    }

    interface ShootingStar {
        x: number;
        y: number;
        vx: number;
        vy: number;
        length: number;
        brightness: number;
        life: number;
        maxLife: number;
    }

    function resizeCanvas() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    function createStars() {
        stars = [];
        const numStars = Math.floor((canvas.width * canvas.height) / 8000);

        for (let i = 0; i < numStars; i++) {
            stars.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                size: Math.random() * 2 + 0.5,
                brightness: Math.random() * 0.8 + 0.2,
                twinkleSpeed: Math.random() * 0.02 + 0.01,
                twinkleOffset: Math.random() * Math.PI * 2
            });
        }
    }

    function createShootingStar() {
        const side = Math.floor(Math.random() * 4);
        let x, y, vx, vy;

        // Xuất hiện từ các cạnh khác nhau
        switch (side) {
            case 0: // Top
                x = Math.random() * canvas.width;
                y = -50;
                vx = (Math.random() - 0.5) * 4;
                vy = Math.random() * 3 + 2;
                break;
            case 1: // Right
                x = canvas.width + 50;
                y = Math.random() * canvas.height;
                vx = -Math.random() * 3 - 2;
                vy = (Math.random() - 0.5) * 4;
                break;
            case 2: // Bottom
                x = Math.random() * canvas.width;
                y = canvas.height + 50;
                vx = (Math.random() - 0.5) * 4;
                vy = -Math.random() * 3 - 2;
                break;
            default: // Left
                x = -50;
                y = Math.random() * canvas.height;
                vx = Math.random() * 3 + 2;
                vy = (Math.random() - 0.5) * 4;
        }

        shootingStars.push({
            x,
            y,
            vx,
            vy,
            length: Math.random() * 60 + 40,
            brightness: Math.random() * 0.8 + 0.2,
            life: 0,
            maxLife: Math.random() * 100 + 50
        });
    }

    function drawStars(time: number) {
        stars.forEach(star => {
            const twinkle = Math.sin(time * star.twinkleSpeed + star.twinkleOffset) * 0.3 + 0.7;
            const alpha = star.brightness * twinkle;

            ctx.beginPath();
            ctx.fillStyle = `rgba(255, 255, 255, ${alpha})`;
            ctx.arc(star.x, star.y, star.size, 0, Math.PI * 2);
            ctx.fill();

            // Thêm hiệu ứng glow cho sao lớn
            if (star.size > 1.5) {
                ctx.beginPath();
                ctx.fillStyle = `rgba(100, 255, 218, ${alpha * 0.3})`;
                ctx.arc(star.x, star.y, star.size * 2, 0, Math.PI * 2);
                ctx.fill();
            }
        });
    }

    function drawShootingStars() {
        shootingStars.forEach((star, index) => {
            const progress = star.life / star.maxLife;
            const fadeIn = Math.min(progress * 4, 1);
            const fadeOut = progress > 0.7 ? (1 - progress) * 3.33 : 1;
            const alpha = star.brightness * fadeIn * fadeOut;

            if (alpha > 0.01) {
                // Vẽ đuôi sao băng
                const gradient = ctx.createLinearGradient(
                    star.x, star.y,
                    star.x - star.vx * star.length / 10, star.y - star.vy * star.length / 10
                );
                gradient.addColorStop(0, `rgba(255, 255, 255, ${alpha})`);
                gradient.addColorStop(0.5, `rgba(100, 255, 218, ${alpha * 0.6})`);
                gradient.addColorStop(1, 'rgba(255, 255, 255, 0)');

                ctx.beginPath();
                ctx.strokeStyle = gradient;
                ctx.lineWidth = 2;
                ctx.moveTo(star.x, star.y);
                ctx.lineTo(
                    star.x - star.vx * star.length / 10,
                    star.y - star.vy * star.length / 10
                );
                ctx.stroke();

                // Vẽ đầu sao băng
                ctx.beginPath();
                ctx.fillStyle = `rgba(255, 255, 255, ${alpha})`;
                ctx.arc(star.x, star.y, 2, 0, Math.PI * 2);
                ctx.fill();

                // Hiệu ứng glow
                ctx.beginPath();
                ctx.fillStyle = `rgba(100, 255, 218, ${alpha * 0.5})`;
                ctx.arc(star.x, star.y, 4, 0, Math.PI * 2);
                ctx.fill();
            }

            // Cập nhật vị trí
            star.x += star.vx;
            star.y += star.vy;
            star.life++;

            // Xóa sao băng đã hết đời
            if (star.life >= star.maxLife ||
                star.x < -100 || star.x > canvas.width + 100 ||
                star.y < -100 || star.y > canvas.height + 100) {
                shootingStars.splice(index, 1);
            }
        });
    }

    function animate(time: number) {
        // Xóa canvas với background đen thuần
        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Vẽ các ngôi sao
        drawStars(time * 0.001);

        // Vẽ sao băng
        drawShootingStars();

        // Tạo sao băng mới ngẫu nhiên - tăng tần suất
        if (Math.random() < 0.012) { // 1.2% chance mỗi frame (tăng từ 0.3%)
            createShootingStar();
        }

        animationId = requestAnimationFrame(animate);
    }

    onMount(() => {
        ctx = canvas.getContext('2d')!;
        resizeCanvas();
        createStars();

        // Tạo nhiều sao băng ban đầu hơn
        for (let i = 0; i < 5; i++) {
            setTimeout(() => createShootingStar(), i * 500);
        }

        animate(0);

        const handleResize = () => {
            resizeCanvas();
            createStars();
        };

        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('resize', handleResize);
        };
    });

    onDestroy(() => {
        if (animationId) {
            cancelAnimationFrame(animationId);
        }
    });
</script>

<canvas
        bind:this={canvas}
        style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; z-index: -1;"
></canvas>