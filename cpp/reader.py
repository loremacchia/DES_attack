import csv
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import pyplot
from mpl_toolkits.mplot3d import Axes3D

dict = {}
with open('./sequential/cppRes.csv') as File:
        reader = csv.reader(File, delimiter=',', quotechar=',',
                            quoting=csv.QUOTE_MINIMAL)
        for row in reader:
            if(not row[0] in dict):
                dict[row[0]] = {}
            if(not '1' in dict[row[0]]):
                dict[row[0]]['1'] = 0
            dict[row[0]]['1'] += float(row[1])

with open('./parallel/parRes.csv') as File:
        reader = csv.reader(File, delimiter=',', quotechar=',',
                            quoting=csv.QUOTE_MINIMAL)
        for row in reader:
            if(not row[1] in dict):
                dict[row[1]] = {}
            if(not row[0] in dict[row[1]]):
                dict[row[1]][row[0]] = 0
            dict[row[1]][row[0]] += float(row[2])
print(dict)

for el in dict:
    for val in dict[el]:
        dict[el][val] /= 20

stri = ""
for el in dict:
    stri += el
    stri += "&"
    for val in dict[el]:
        stri += str(dict[el][val])
        stri += "&"
    stri += "\n"
print(stri)