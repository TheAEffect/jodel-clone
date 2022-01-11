<script>
    import Icon from 'svelte-awesome';
    import {faLink} from '@fortawesome/free-solid-svg-icons'

    export const color = undefined;
    export let prop = {
        value: undefined
    };

    /**
     * Passes the link to the parent
     */
    const handleLink = () => {
        prop = {...prop, valid: isValidURL(prop.value)};
    }

    /**
     * Checks if the given string is a vlid url
     * @param string
     * @returns {boolean}
     */
    const isValidURL = (string) => {
        let res = string.match(/(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/g);
        return (res !== null)
    };
</script>
    <div class="create-link">
        <div>
            <span class:input="{prop.value?.length > 0}">
                <Icon data={faLink}/>
            </span>
            <input
                    placeholder="linkeinfügen"
                    id="jodel-link"
                    bind:value={prop.value}
                    on:input={handleLink}
                    name="text"
            />
        </div>
        {#if !prop?.valid && prop?.valid !== undefined && prop?.valid !== ''}
            <div>
                Diese Domain wird nicht unterstützt 👀
            </div>
        {/if}
    </div>