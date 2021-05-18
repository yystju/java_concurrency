#pragma once

#include <thread>
#include <deque>
#include <chrono>
#include <mutex>
#include <condition_variable>

template<class K, class V>
class Cache {
public:
    virtual V get(long verbose, K key);
};

template<class K, class V>
class DataSource {
public:
    virtual V fetch(long verbose, K key);
};

template<class T>
class ConcurrentQueue {
public:
    ConcurrentQueue(int size, long interval = 200) : size(size), interval(interval) {}
    ~ConcurrentQueue() {}

    void push(T&& t) {
        std::unique_lock<std::mutex> lock(this->mutex);

        while(data.size() >= size) {
            cv_full.wait_for(lock, std::chrono::milliseconds(this->interval));
        }

        data.push_back(t);

        cv_empty.notify_all();
    }

    T pop() {
        std::unique_lock<std::mutex> lock(this->mutex);

        while(data.size() == 0) {
            cv_empty.wait_for(lock, std::chrono::milliseconds(this->interval));
        }

        T t = data.front();

        data.pop_front();

        cv_full.notify_all();

        return t;
    }
private:
    int size;
    long interval;
    std::deque<T> data;
    std::mutex mutex;
    std::condition_variable cv_empty;
    std::condition_variable cv_full;
};