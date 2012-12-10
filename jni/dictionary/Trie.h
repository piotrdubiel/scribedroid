/*
 * Trie.h
 *
 *  Created on: 2012-05-23
 *      Author: piotrek
 */

#ifndef TRIE_H_
#define TRIE_H_

#include <vector>

class Trie {
public:
	virtual void add(std::string) = 0;
	virtual std::vector<std::string> suggest(std::string,int) const = 0;
	virtual std::vector<std::string> suggest(std::string) const = 0;
	virtual bool isValid(std::string) const = 0;
};

#endif /* TRIE_H_ */
