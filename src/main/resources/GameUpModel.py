#!/usr/bin/env python
# coding: utf-8

# In[154]:


import pyodbc
import time

server = 'codiecon.database.windows.net'
database = 'codeicon'
username = 'satish@codiecon'
password = 'Nitssats123'
driver= '/usr/local/lib/libmsodbcsql.17.dylib'
cnxn = pyodbc.connect('DRIVER='+driver+';SERVER='+server+';PORT=1433;DATABASE='+database+';UID='+username+';PWD='+ password)
cursor = cnxn.cursor()
cursor.execute("select email,age_grp,gender,indoor, outdoor, game_type, game_time from dbo.tournament_booked right outer join dbo.users on dbo.tournament_booked.user1_email = dbo.users.email;")
row = cursor.fetchone()

with open('data.csv', 'w') as the_file:
    the_file.write('userId#age_grp#gender#indoor#outdoor#game_type#game_time\n')
    while row:
        print (str(row[0]) + "#" + str(row[1]) + "#" + str(row[2]) + "#" + str(row[3]) + "#" + str(row[4]) + "#" + str(row[5]) + "#" + str(row[6]))
        the_file.write(str(row[0]) + "#" + str(row[1]) + "#" + str(row[2]) + "#" + str(row[3]) + "#" + str(row[4]) + "#" + str(row[5]) +  "#" + str(row[6]) + "\n")
        row = cursor.fetchone()


# In[155]:


import pandas as pd


# In[156]:


data = pd.read_csv("data.csv", sep='#',header='infer')


# In[157]:


data.head()


# In[158]:


data['gender'] = data['gender'].map({'M': 0, 'F': 1, 'm': 0 , 'F': 1})
data['age_grp'] = data['age_grp'].map({'1-5': 0, '5-10': 1, '11-15': 2 , '15-20': 3,'21-25': 4, '25-30': 5, '31-35': 6 , '35-40': 7,'41-45': 8, '45-50': 9, '51-55': 10 , '55-60': 11})


# In[159]:


data.head(100)


# In[160]:


games = ['table tennis','chess','badminton','cricket']
current_time = lambda: int(round(time.time() * 1000))
#last 10 days moving window
t = 864000000

def flaten(indoor, outdoor, game_type, game_time,type_of_game):
    game = []
    for i in indoor.split(","):
        game.append(i.lower())
    for i in outdoor.split(","):
        game.append(i.lower())

    if(game_type != 'None' and game_time != 'None' and game_time != 'game_time' and (int(current_time())-int(game_time)) < t):
        game.append(game_type.lower())
    return game.count(type_of_game)

for g in games:
    data[g] = data.apply(lambda x: flaten(x['indoor'],x['outdoor'],x['game_type'],x['game_time'],g), axis=1)

data = data.drop(columns=['indoor', 'outdoor','game_type','game_time'])


# In[161]:


data = data.dropna(how='any')
data.head(100)


# In[162]:


import matplotlib.pyplot as plt
import numpy as np
get_ipython().run_line_magic('matplotlib', 'inline')

import random
co = ['r','g','b','r','g','b']
for g in games:
    plt.figure(figsize=(40,10))
    plt.bar(data['userId'],data[g],color=co[random.randint(0,5)],label = g)
    plt.legend(loc=1, prop={'size': 30})
    plt.show()


# In[163]:


from sklearn.cluster import KMeans
data = data.drop(columns=['userId'])
kmeans = KMeans(n_clusters=5, random_state=0).fit(data)


# In[164]:


kmeans.labels_


# In[ ]:




