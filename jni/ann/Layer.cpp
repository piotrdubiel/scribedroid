#include "layer.h"

#include <iostream>

Layer::Layer(int count,int inserts) {
    for (int i=0; i < count; ++i)
        neurons.push_back(Neuron(inserts));
}

vector<float> Layer::classify(vector<float> x) const {
    vector<float> result;
    deque<Neuron>::const_iterator i;
    for (i=neurons.begin(); i != neurons.end(); ++i)
        result.push_back((*i).answer(x));

    return result;
}

vector<float> Layer::get_dy(vector<float> x) const {
    vector<float> result;
    deque<Neuron>::const_iterator i;
    for (i=neurons.begin();i!=neurons.end();++i)
        result.push_back((*i).answer_dy(x));

    return result;
}

float Layer::get_weight(int neuron, int input) const {
    return neurons[neuron].get_weight(input);
}

int Layer::size() const {
    return neurons.size();
}

void Layer::modify(int neuron, vector<float> mod) {
    neurons[neuron].modify(mod);
}

void Layer::load(vector<vector<float> > w) {
	//if (w.size() != neurons.size()) throw exception();
	for (int i=0; i < neurons.size(); ++i) {
		neurons[i].load(w[i]);
	}
}

void Layer::printout() const {
	cout<<"Layer"<<endl;
	for (deque<Neuron>::const_iterator i=neurons.begin();i!=neurons.end();++i) {
		(*i).printout();
		cout<<endl;
	}
}
