import axios from 'axios';

/**
 * Defines underlying values for Axios
 * @returns {Promise<AxiosResponse<any>>}
 */
const axiosAPI = () => {
    return axios.create({
        baseURL: `${process.env.SERVER_HOST}:${process.env.SERVER_PORT}`,
        withCredentials: true,
        headers: {
            'Accept': 'application/json',
        },
    });
};

/**
 * Request to get all posts
 * @returns {Promise<AxiosResponse<any>>}
 */
const getPosts = () => axiosAPI().get('/posts')
    .then(res => {
        if (res.status === 200) {
            return res.data;
        }
        return [];
    })
    .catch(error => {
        throw error;
    });

/**
 * Request to get all posts
 * @returns {Promise<AxiosResponse<any>>}
 */
const getPost = async (id) => axiosAPI().get('/post/' + id)


/**
 * Request to delete specific post
 * @returns {Promise<AxiosResponse<any>>}
 */
const deletePost = (id) => axiosAPI().delete('/post/' + id)
    .then(res => {
        return res.status === 200;

    })
    .catch(error => {
        throw error;
    });

/**
 * Request to delete specific comment
 * @returns {Promise<AxiosResponse<any>>}
 */
const deleteComment = (id) => axiosAPI().delete('/comment/' + id)
    .then(res => {
        return res.status === 200;

    })
    .catch(error => {
        throw error;
    });

export {
    axiosAPI,
    getPost,
    getPosts,
    deletePost,
    deleteComment
};