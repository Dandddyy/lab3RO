package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

func smoking(component int, table *[]bool, SmokingSemaphore chan bool, DealingSemaphore chan bool, waitGroup *sync.WaitGroup) {
	for {
		SmokingSemaphore <- true
		if !(*table)[component] {
			fmt.Println("Курить ", component)
			for i := range *table {
				(*table)[i] = false
			}
			time.Sleep(time.Second * 2)
			DealingSemaphore <- true
		} else {
			<-SmokingSemaphore
		}
	}
	waitGroup.Done()
}

func controller(table *[]bool, SmokingSemaphore chan bool, DealingSemaphore chan bool, waitGroup *sync.WaitGroup) {
	for {
		<-DealingSemaphore
		var firstcomponent, secondcomponent = getComponent()
		fmt.Println("На столі:", firstcomponent, "і", secondcomponent)
		(*table)[firstcomponent] = true
		(*table)[secondcomponent] = true
		<-SmokingSemaphore
	}
	waitGroup.Done()
}

func getComponent() (int, int) {
	component1 := rand.Intn(3)
	component2 := rand.Intn(3)
	for component2 == component1 {
		component2 = rand.Intn(3)
	}

	return component1, component2
}

func main() {
	var table = make([]bool, 3)
	var waitGroup sync.WaitGroup
	var SmokingSemaphore = make(chan bool)
	var DealingSemaphore = make(chan bool, 1)

	DealingSemaphore <- true
	waitGroup.Add(1)
	go controller(&table, SmokingSemaphore, DealingSemaphore, &waitGroup)

	for i := 0; i < 3; i++ {
		waitGroup.Add(1)
		go smoking(i, &table, SmokingSemaphore, DealingSemaphore, &waitGroup)
	}

	waitGroup.Wait()
}