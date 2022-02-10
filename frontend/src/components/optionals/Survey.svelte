<script>
    import Icon from 'svelte-awesome';
    import {faPlus, faTimesCircle} from '@fortawesome/free-solid-svg-icons'

    export const color = undefined;
    export let prop = {
        addChoice: true,
        choices: [
            {
                value: null
            },
            {
                value: null
            }
        ]
    };

    /**
     * handles the click on add choice
     */
    const handleAddChoice = () => {
        if(prop.choices.length < 4) {
            prop.choices = [...prop.choices, {value: null, removable: true}]
        }
        if(prop.choices.length === 4) {
            prop.addChoice = false;
        }
    };
</script>
<slot></slot>
<div class="survey">
    {#each prop.choices as choice, i}
        <div style="position:relative;display:flex;justify-content: center;align-items: center ">
            <input bind:value={choice.value}
                   maxlength="15"
                   placeholder="Option {i+1}"/>
            {#if choice.removable}
                <span class="survey-times-circle"
                      on:click={()=>{prop.choices = prop.choices.filter(m => m !== choice);prop.addChoice = true;}}>
                    <Icon data={faTimesCircle}/>
                </span>
            {/if}
        </div>
    {/each}
    {#if prop.addChoice}
        <div class="survey-addChoice"
             on:click={handleAddChoice}>
            <Icon data={faPlus}/> Option hinzufügen
        </div>
    {/if}
    <div class="survey-info">
        Umfragen dauern 24 Stunden
    </div>
</div>