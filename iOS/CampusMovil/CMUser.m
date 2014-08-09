//
//  CMUser.m
//  CampusMovil
//
//  Created by Mateo Olaya Bernal on 8/8/14.
//  Copyright (c) 2014 Mateo Olaya Bernal. All rights reserved.
//

#import "CMUser.h"

@implementation CMUser
@synthesize username;
@synthesize email;
@synthesize token = _token;
@synthesize expires = _expires;

- (instancetype)initWithToken:(NSString *)token lifetime:(NSDate *)lifetime {
    self = [super init];
    
    if (self) {
        _token = token;
        _expires = lifetime;
    }
    
    return self;
}
@end
