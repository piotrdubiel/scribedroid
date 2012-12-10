#ifndef NETWORK_H
#define NETWORK_H

#include <deque>
#include <vector>
#include <iostream>
#include "layer.h"

using namespace std;

class Network
{
public:
    Network(vector<int> arch);

    vector<float> classify(vector<float> x) const;
    void train(vector<float> x,int ident,float beta);
    void train(vector<float> x,vector<float> w,float beta);
    void load(string filename);
    void load(vector<vector<vector<float> > > w);

    void printout() const;
private:
    deque<Layer> layers;
};

#endif // NETWORK_H
