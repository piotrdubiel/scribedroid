#ifndef _TRIE_H
#define	_TRIE_H

#include <map>
#include <vector>
#include <string>

class BasicTrie {
private:
	class Node {
	public:
		char character;
		int priority;
		std::map<char,Node*> nodes;

		Node(char c) {
			character=c;
			priority=-1;
		}

		~Node();

		void save(std::ofstream&) const;
		void load(std::ifstream&);
	};
	struct PriorityString {
	public:
		std::string str;
		int priority;


		PriorityString(std::string s,int p): str(s), priority(p){};
	};

	class PriorityStringComp {
	public:
		bool operator()(const struct PriorityString& a,const struct PriorityString& b) {
			return a.priority>b.priority;
		}
	};

	Node* root;

	Node* search(std::string) const;
public:
	int count;

	BasicTrie();
	~BasicTrie ();
	void dump();

	void add(std::string);
	std::vector<std::string> suggest(std::string) const;
	std::vector<std::string> suggest(std::string,int) const;
	bool isValid(std::string) const;
	void save(std::ofstream&) const;
	void load(std::ifstream&);
};

#endif
