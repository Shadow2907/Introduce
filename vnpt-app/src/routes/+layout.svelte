<!-- src/routes/+layout.svelte -->
<script>
    import '../app.css';

    // Optional: Add any global state or shared logic here
    let { children } = $props();
</script>

<svelte:head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="icon" href="/favicon.ico" />
</svelte:head>

<div class="app">
    {@render children()}
</div>

<style>
    .app {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
    }
</style>