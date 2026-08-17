data: list[list[str]] = []

with open("emojis", 'r') as f:
    for line in f.read().split("\n"):
        elm=line.split("=")
        if "-" in elm[0]:
            continue
        data.append(elm)

n = 0

with open("../comitas-api/src/main/java/cloud/thehsi/ComitasBotJ/API/Discord/Emoji/Emojis/Emojis.java", 'w') as f:
    f.write("""package cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emojis;
    
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class Emojis {\n""")

for i in range(0, len(data), 500):
    with open("../comitas-api/src/main/java/cloud/thehsi/ComitasBotJ/API/Discord/Emoji/Emojis/IEmojisData" + str(n) + ".java", 'w') as f:
        f.write("""package cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emojis;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;

@SuppressWarnings("ALL")
class IEmojisData""" + str(n) + " {\n")
    # noinspection PyTypeHints
    for elm in data[i:i+500]:
        f.write("    static final Emoji " + elm[0] + " = Emoji.fromUnicode(" + elm[1] + ");\n")
    f.write("}")
    
    with open("../comitas-api/src/main/java/cloud/thehsi/ComitasBotJ/API/Discord/Emoji/Emojis/Emojis.java", 'a') as f:
        # noinspection PyTypeHints
        for elm in data[i:i+500]:
            f.write("    @NotNull public static final Emoji " + elm[0] + " = IEmojisData" + str(n) + "." + elm[0] + ";\n")
    n+=1

with open("../comitas-api/src/main/java/cloud/thehsi/ComitasBotJ/API/Discord/Emoji/Emojis/Emojis.java", 'a') as f:
    f.write("}")

print("Generated Emojis")