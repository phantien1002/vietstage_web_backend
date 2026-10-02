import re

filepath = 'src/main/java/com/example/vietstage_web_be/controller/AdminController.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# The python script might have appended the method to every "}" or messed up the string
# I will just revert AdminController to an earlier state or fix it.
# Let's see the end of AdminController.
