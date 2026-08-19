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

import java.util.Objects;

@SuppressWarnings({"ALL"})
public class Emojis {\n""")
    for elm in data:
        f.write("    @NotNull public static Emoji " + elm[0] + "() { return Objects.requireNonNull(Emoji.fromUnicode(" + elm[1] + ")); }\n")
    f.write("}")

print("Generated Emojis")