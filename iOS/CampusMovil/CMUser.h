//
//  CMUser.h
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/8/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CMUser : NSObject
@property (nonatomic, strong) NSString * username;
@property (nonatomic, strong) NSString * email;
@property (nonatomic, strong, readonly) NSString * token;
@property (nonatomic, strong, readonly) NSDate * expires;

- (instancetype)initWithToken:(NSString *)token lifetime:(NSDate *)lifetime;
@end
