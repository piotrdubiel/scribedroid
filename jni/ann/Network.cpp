#include "network.h"

Network::Network(vector<int> arch)
{
	for (unsigned int i=1; i < arch.size(); ++i)
	{
        layers.push_back(Layer(arch[i],arch[i-1]));
	}
	
	printout();
}

vector<float> Network::classify(vector<float> x) const
{
    vector<float> result=x;
    deque<Layer>::const_iterator i;
    for (i=layers.begin(); i != layers.end(); ++i) {
        result=(*i).classify(result);
    }

    return result;
}

void Network::train(vector<float> x,int ident,float beta)
{
    vector<vector<float> > y;
    vector<vector<float> > dy;

    vector<float> answer=x;

    vector<vector<float> > delta(layers.size(),vector<float>());

    deque<Layer>::iterator i;
    for (i = layers.begin(); i != layers.end(); ++i)
    {
        dy.push_back((*i).get_dy(answer));
        answer=(*i).classify(answer);
        y.push_back(answer);
    }

    vector<float> d(x.size(),-1.0f);
    d[ident] = 1.0f;

    //output layer
    for (int j = 0; j < y.back().size(); ++j)
        delta.back().push_back((d[j]-y.back()[j])*dy.back()[j]);


    //hidden layers
    for (int k=layers.size()-1;k>=0;--k)
        for (int j=0;j<y[k].size();++j) {
            float sum=0.0;
            for (int l=0;l<layers[k+1].size();++l)
            {
                sum+=delta[k+1][l]*layers[k+1].get_weight(l,j);
            }
            delta[k].push_back(sum*dy[k][j]);
        }

	cout<<"DELTA"<<endl;
	for (vector<vector<float> >::iterator i=delta.begin();i!=delta.end();++i)
	{
		for (vector<float>::iterator j=(*i).begin();j!=(*i).end();++j)
			cout<<(*j)<<" ";
		cout<<endl;
	}

    //correction
    for (int k=1;k<layers.size();++k)
        for (int n=0;n<layers[k].size();++n)
        {
            vector<float> mod;
            for (int l=0;l<layers[k-1].size();++l)
                mod.push_back(beta*delta[k][n]*y[k-1][l]);

            mod.push_back(beta*delta[k][n]);
            layers[k].modify(n,mod);
        }
    printout();
}

void Network::train(vector<float> x,vector<float> d,float beta)
{
	vector<vector<float> > y;
	vector<vector<float> > dy;

	vector<float> answer=x;

	vector<vector<float> > delta(layers.size(),vector<float>());

	for (deque<Layer>::iterator i=layers.begin();i!=layers.end();++i) {
		dy.push_back((*i).get_dy(answer));
		answer=(*i).classify(answer);
		y.push_back(answer);
		cout<<"Answer ";
		for (vector<float>::iterator it=answer.begin();it!=answer.end();++it)
			cout<<(*it)<<" ";
		cout<<endl;
	}

	for (unsigned int i=0;i<y.size();++i) {
		delta.back().push_back((d[i]-y.back()[i])*dy.back()[i]);
	}

	for (unsigned int i=layers.size()-2;i>=0;i--) {

	}
}

void Network::load(vector<vector<vector<float> > > w) {
	//if (w.size() != layers.size()) throw exception();
	for (int i=0;i<layers.size();++i)
	{
			layers[i].load(w[i]);
	}
}

void Network::printout() const {
	cout<<"LAYERS "<<layers.size()<<endl;
	for (deque<Layer>::const_reverse_iterator i=layers.rbegin();i!=layers.rend();++i)
		(*i).printout();
	cout<<endl;
}

