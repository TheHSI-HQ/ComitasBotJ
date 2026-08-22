package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ActionRowComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;

public interface IActionRowComponent extends ActionRowComponent {
    ActionRowChildComponent getAsActionRowChildComponent();
}
