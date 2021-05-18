#include <iostream>

#include "concurrency.hpp"

int main(int argc, char * argv[]) {
    ConcurrentQueue<int> queue(3);

    std::thread t1([&queue] () {
        std::cout << "[producer]\n";
        for(int i = 0; i < 10; ++i) {
            queue.push(std::move(i));
        }
    });
    std::thread t2([&queue] () {
        std::cout << "[consumer]\n";
        for(int i = 0; i < 10; ++i) {
            std::cout << "pop : " << queue.pop() << "\n";
        }
    });

    t1.join();
    t2.join();

    return 0;
}
