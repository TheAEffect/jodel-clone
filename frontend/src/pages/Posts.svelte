<script>
    import {axiosAPI} from "../apis/AxiosQuarkus.js";
    import CreatePost from "../components/posts/CreatePost.svelte";
    import PostList from "../components/posts/PostList.svelte";
    import {storeGeo, storeChannel, storeOrder } from "../utils/store";
    import Post from "../components/posts/Post.svelte";

    export let singlePost=undefined;
    export let id=undefined;
    export let rndColor = undefined;
    let selected;
    let loaded;
    let bgColor = null;
    let state = {
        posts: null,
        createPost: false,
        geo: null
    }

    /**
     * Gets all posts sorted by number of date (default)
     */
    const getPostsDefault = () => {
        let channel = $storeChannel?.id? '&channel='+$storeChannel?.id : ''
        axiosAPI().get('/post?longitude=' + $storeGeo.longitude + '&latitude=' + $storeGeo.latitude+channel)
            .then((res) => {
                if (res.status === 200) {
                    state.posts = res.data;
                }
            });
    }

    /**
     * Gets all posts sorted by number of comments
     */
    const getCommentPosts = () => {
        let channel = $storeChannel?.id? '&channel='+$storeChannel?.id : ''
        axiosAPI().get('/post?longitude=' + $storeGeo.longitude + '&latitude=' + $storeGeo.latitude + '&criteria=comments' + channel)
            .then((res) => {
                if (res.status === 200) {
                    state.posts = res.data;
                }
            });
    }

    /**
     * Gets all posts sorted by votes
     */
    const getVotePosts = () => {
        let channel = $storeChannel?.id? '&channel='+$storeChannel?.id : ''
        axiosAPI().get('/post?longitude=' + $storeGeo.longitude + '&latitude=' + $storeGeo.latitude + '&criteria=votes'+channel)
            .then((res) => {
                if (res.status === 200) {
                    state.posts = res.data;
                }
            });
    }

    /**
     * Fetches one post at singlePost, otherwise all posts
     */
    const fetchPosts = () => {
        if (singlePost) {
            axiosAPI().get('/post/' + id)
                .then((res) => {
                    if (res.status === 200) {
                        state.posts = res.data;
                        bgColor = (res.data) ? res.data.color : null;
                    }
                });
        } else {
            getPosts()
        }
    }

    /**
     * Calls function according to which the posts should be sorted
     */
    const getPosts =() => {
        if($storeOrder === 3) {
            getVotePosts()
        } else if($storeOrder === 2) {
            getCommentPosts()
        } else {
            getPostsDefault()
        }
    }

    /**
     * Load posts if geo changes
     */
    $: {
        if ($storeGeo && !loaded) {
            loaded = true;
            fetchPosts();
        }
    }

</script>
{#if state.createPost}
    <CreatePost bind:createPost={state.createPost} bind:posts={state.posts} rndColor={rndColor}/>
{:else if state.posts}
    {#if singlePost}
        <Post post={state.posts} singlePost={singlePost}/>
    {:else}
        <PostList bind:selected={selected} bind:posts={state.posts} bind:createPost={state.createPost} color={rndColor} getPosts={getPosts}/>
    {/if}
{/if}




