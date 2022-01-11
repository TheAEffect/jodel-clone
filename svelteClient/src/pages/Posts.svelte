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
     * Load posts if geo changes
     */

    $: {
        if ($storeGeo && !loaded) {
            loaded = true;
            fetchPosts();
        }
    }

    const getCommentPosts = () => {
        let channel = $storeChannel?.id? '&channel='+$storeChannel?.id : ''
        axiosAPI().get('/post?longitude=' + $storeGeo.longitude + '&latitude=' + $storeGeo.latitude + '&criteria=comments' + channel)
            .then((res) => {
                if (res.status === 200) {
                    state.posts = res.data;
                }
            });
    }

    const setOrderAndFetch = () => {
        switch ($storeOrder) {
            case 3:
                getPosts();
                break;
            case 2:
                getPosts();
                break;
            default:
                getPosts();
                break;
        }
    }

    const getPosts =() => {
        if($storeOrder === 3) {
            getVotePosts()
        } else if($storeOrder === 2) {
            getCommentPosts()
        } else {
            getPostsDefault()
        }
    }

    const getPostsDefault = () => {
        let channel = $storeChannel?.id? '&channel='+$storeChannel?.id : ''
        axiosAPI().get('/post?longitude=' + $storeGeo.longitude + '&latitude=' + $storeGeo.latitude+channel)
            .then((res) => {
                if (res.status === 200) {
                    state.posts = res.data;
                }
            });
    }

    const getVotePosts = () => {
        let channel = $storeChannel?.id? '&channel='+$storeChannel?.id : ''
        axiosAPI().get('/post?longitude=' + $storeGeo.longitude + '&latitude=' + $storeGeo.latitude + '&criteria=votes'+channel)
            .then((res) => {
                if (res.status === 200) {
                    state.posts = res.data;
                }
            });
    }

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

</script>
{#if state.createPost}
    <CreatePost bind:createPost={state.createPost} bind:posts={state.posts} rndColor={rndColor}/>
{:else if state.posts}
    {#if singlePost}
        <Post post={state.posts} singlePost={singlePost}/>
    {:else}
        <PostList bind:selected={selected} bind:posts={state.posts} bind:createPost={state.createPost} color={rndColor} setOrderAndFetch={setOrderAndFetch}/>
    {/if}
{/if}




