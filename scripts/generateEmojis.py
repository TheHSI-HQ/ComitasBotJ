data: list[list[str]] = []

with open("emojis", 'r') as f:
    for line in f.read().split("\n"):
        elm=line.split("=")
        if "-" in elm[0]:
            continue
        data.append(elm)

n = 0

with open("Emojis.java", 'w') as f:
    f.write("""package cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emojis;
    
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class Emojis {\n""")

for i in range(0, len(data), 500):
    with open("IEmojisData" + str(n) + ".java", 'w') as f:
        f.write("""package cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emojis;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;

@SuppressWarnings("ALL")
class IEmojisData""" + str(n) + " {\n")
        for elm in data[i:i+500]:
            f.write("    static final Emoji " + elm[0] + " = Emoji.fromUnicode(" + elm[1] + ");\n")
        f.write("}")
    
    with open("Emojis.java", 'a') as f:
            for elm in data[i:i+500]:
                f.write("    public static final Emoji " + elm[0] + " = IEmojisData" + str(n) + "." + elm[0] + ";\n")
    n+=1

with open("Emojis.java", 'a') as f:
    f.write("}")